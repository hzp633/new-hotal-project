# -*- coding: utf-8 -*-
"""
drawio 预览 & 几何校验
- 按 .drawio 中的真实坐标，用 Graphviz(neato -n, 固定坐标) 渲染 PNG 预览
- 同时做几何校验：越界 / 重叠 / 文字宽度估算
用法：python preview_drawio.py
"""
import os
import re
import glob
import subprocess
import xml.etree.ElementTree as ET

BASE = os.path.dirname(os.path.abspath(__file__))
DRAWIO_DIR = os.path.join(BASE, "drawio")
PREVIEW_DIR = os.path.join(BASE, "preview")
DOT = os.path.join(BASE, "gv", "Graphviz-12.2.1-win64", "bin", "neato.exe")
os.makedirs(PREVIEW_DIR, exist_ok=True)

NS = "{http://www.w3.org/2000/svg}"


def attr(style, key, default=None):
    m = re.search(r"(?:^|;)%s=([^;]*)" % re.escape(key), style or "")
    return m.group(1) if m else default


def is_ellipse(style):
    return (style or "").startswith("ellipse")


def load(path):
    root = ET.parse(path).getroot()
    model = root.find(".//mxGraphModel")
    pw = int(model.get("pageWidth"))
    ph = int(model.get("pageHeight"))
    cells = {}
    edges = []
    for c in model.iter("mxCell"):
        cid = c.get("id")
        if c.get("vertex") == "1":
            g = c.find("mxGeometry")
            cells[cid] = dict(
                id=cid, parent=c.get("parent"), value=c.get("value") or "",
                style=c.get("style") or "",
                x=float(g.get("x") or 0), y=float(g.get("y") or 0),
                w=float(g.get("width") or 0), h=float(g.get("height") or 0))
        elif c.get("edge") == "1":
            edges.append(dict(source=c.get("source"), target=c.get("target"),
                              value=c.get("value") or "", style=c.get("style") or ""))
    return pw, ph, cells, edges


def absolute(cells, cid, acc=None):
    acc = acc or []
    c = cells[cid]
    p = c["parent"]
    if p in cells:
        px, py = absolute(cells, p, acc)
        return px + c["x"], py + c["y"]
    return c["x"], c["y"]


def check(path, pw, ph, cells, edges):
    print("\n--- %s  (页面 %dx%d) ---" % (os.path.basename(path), pw, ph))
    problems = []
    abs_pos = {}
    for cid, c in cells.items():
        ax, ay = absolute(cells, cid)
        abs_pos[cid] = (ax, ay, c["w"], c["h"])

    # 1) 越界检查（顶层元素）
    for cid, c in cells.items():
        if c["parent"] != "1":
            continue
        ax, ay, w, h = abs_pos[cid]
        if ax < 0 or ay < 0 or ax + w > pw or ay + h > ph:
            problems.append("越界: %s (%s) at (%.0f,%.0f) %.0fx%.0f" % (cid, c["value"][:14], ax, ay, w, h))

    # 2) 同层重叠检查
    def overlap(a, b):
        ax, ay, aw, ah = a
        bx, by, bw, bh = b
        return ax < bx + bw and bx < ax + aw and ay < by + bh and by < ay + ah

    groups = {}
    for cid, c in cells.items():
        groups.setdefault(c["parent"], []).append(cid)
    for p, ids in groups.items():
        for i in range(len(ids)):
            for j in range(i + 1, len(ids)):
                if overlap(abs_pos[ids[i]], abs_pos[ids[j]]):
                    problems.append("重叠: %s <-> %s" % (ids[i], ids[j]))

    # 3) 子元素是否超出父容器
    for cid, c in cells.items():
        p = c["parent"]
        if p not in cells:
            continue
        px, py, pw_, ph_ = abs_pos[p]
        ax, ay, w, h = abs_pos[cid]
        if ax < px - 0.5 or ay < py - 0.5 or ax + w > px + pw_ + 0.5 or ay + h > py + ph_ + 0.5:
            problems.append("溢出父容器: %s (%s) in %s" % (cid, c["value"][:14], p))

    # 4) 文字宽度估算（中文按 1.0*fontSize，其余按 0.55*fontSize）
    for cid, c in cells.items():
        if not c["value"] or cid in ("title", "top"):
            continue
        style = c["style"] or ""
        if "umlActor" in style:
            continue
        if c["w"] < 60:          # 竖排文字叶子节点，宽度不受限
            continue
        fs = float(attr(style, "fontSize", "12"))
        seg = [re.sub(r"<[^>]+>", "", s) for s in re.split(r"<br\s*/?>", c["value"])]
        need = max(sum(1.0 if ord(ch) > 127 else 0.55 for ch in s) for s in seg) * fs
        avail = c["w"] * (0.72 if is_ellipse(style) else 1.0) - 10
        if need > avail:
            problems.append("文字可能溢出: \"%s\" 需≈%.0fpx / 可用%.0fpx"
                            % (max(seg, key=len)[:16], need, avail))

    if problems:
        for p in problems:
            print("   [!] " + p)
    else:
        print("   [OK] 无越界 / 无重叠 / 无文字溢出")
    return problems


def render(path, pw, ph, cells, edges):
    name = os.path.splitext(os.path.basename(path))[0]
    gv_path = os.path.join(PREVIEW_DIR, name + ".gv")
    png_path = os.path.join(PREVIEW_DIR, name + ".png")

    abs_pos = {cid: absolute(cells, cid) for cid in cells}
    out = ["digraph G {",
           '  graph [bb="0,0,%d,%d", dpi=72, splines=false, outputorder=edgesfirst];' % (pw, ph),
           '  node [fontname="Microsoft YaHei", fontsize=10, fixedsize=true, shape=box, style="filled"];',
           '  edge [penwidth=0.9, color="#8FA8C8", arrowsize=0.6];']

    def pid(cid):
        return "n_" + re.sub(r"\W", "_", cid)

    for cid, c in cells.items():
        if cid in ("0", "1", "title"):
            continue
        ax, ay = abs_pos[cid]
        cx, cy = ax + c["w"] / 2.0, ay + c["h"] / 2.0
        ny = ph - cy
        style = c["style"]
        shape = "ellipse" if is_ellipse(style) else "box"
        fill = attr(style, "fillColor", "#FFFFFF") or "#FFFFFF"
        stroke = attr(style, "strokeColor", "#666666") or "#666666"
        if fill == "none":
            fill = "#FFFFFF"
        fontcolor = attr(style, "fontColor", "#333333") or "#333333"
        fs = float(attr(style, "fontSize", "12"))
        label = re.sub(r"<[^>]+>", "", c["value"]).replace('"', "'")
        labelloc = ""
        if "umlActor" in style:
            shape, label = "box", label
        elif "swimlane" in style or "verticalAlign=top" in style:
            labelloc = ', labelloc="t"'
        out.append(
            '  %s [label="%s", pos="%.2f,%.2f!", width=%.4f, height=%.4f, '
            'shape=%s, fillcolor="%s", color="%s", fontcolor="%s", fontsize=%.1f%s];'
            % (pid(cid), label, cx, ny, c["w"] / 72.0, c["h"] / 72.0, shape, fill, stroke, fontcolor, fs, labelloc))

    for i, e in enumerate(edges):
        if e["source"] in cells and e["target"] in cells:
            out.append('  %s -> %s [label="%s"];'
                       % (pid(e["source"]), pid(e["target"]), e["value"]))
    out.append("}")

    with open(gv_path, "w", encoding="utf-8") as f:
        f.write("\n".join(out))

    subprocess.run([DOT, "-n", "-Tpng", "-Gdpi=72", gv_path, "-o", png_path],
                   check=True, capture_output=True)
    return png_path


def main():
    total = 0
    for path in sorted(glob.glob(os.path.join(DRAWIO_DIR, "*.drawio"))):
        pw, ph, cells, edges = load(path)
        total += len(check(path, pw, ph, cells, edges))
        png = render(path, pw, ph, cells, edges)
        print("   预览 -> %s" % os.path.basename(png))
    print("\n合计问题数：%d" % total)


if __name__ == "__main__":
    main()
