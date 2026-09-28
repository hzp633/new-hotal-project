# -*- coding: utf-8 -*-
"""
酒店预约管理系统 — draw.io (diagrams.net) 图形生成脚本
风格：树形功能模块结构图 + 分层用例图（黑白线框）
生成：前台模块图 / 后台模块图 / 管理员用例图 / 用户用例图
用法：python gen_drawio.py
产物：drawio/*.drawio（可直接用 draw.io 打开编辑）
"""
import os
from collections import defaultdict
from xml.sax.saxutils import escape

BASE = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(BASE, "drawio")
os.makedirs(OUT_DIR, exist_ok=True)

LINE = "#333333"
FONT = "#000000"

TITLE_STYLE = ("text;html=1;strokeColor=none;fillColor=none;align=center;"
               "verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=15;"
               "fontStyle=1;fontColor=#333333;")

TOP_STYLE = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=%s;"
             "fontColor=%s;fontSize=13;fontStyle=1;" % (LINE, FONT))
MOD_STYLE = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=%s;"
             "fontColor=%s;fontSize=11;" % (LINE, FONT))
LEAF_STYLE = ("rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=%s;"
              "fontColor=%s;fontSize=10;" % (LINE, FONT))
TREE_EDGE = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=none;strokeColor=%s;"
             "exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" % LINE)

ACTOR_STYLE = ("shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;"
               "outlineConnect=0;fillColor=#FFFFFF;strokeColor=%s;fontColor=%s;fontSize=12;"
               % (LINE, FONT))
UC_STYLE = ("ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=%s;"
            "fontColor=%s;fontSize=11;" % (LINE, FONT))
ASSOC_EDGE = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=none;strokeColor=" + LINE + ";"
              "exitX=1;exitY={frac:.2f};exitDx=0;exitDy=0;entryX=0;entryY=0.5;"
              "entryDx=0;entryDy=0;")
INCLUDE_EDGE = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;dashed=1;endArrow=open;"
                "endFill=0;strokeColor=%s;fontColor=#333333;fontSize=10;"
                "exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;"
                "entryDx=0;entryDy=0;" % LINE)
# extend：箭头由「扩展用例」指向「基用例」，方向与 include 相反
EXTEND_EDGE = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;dashed=1;endArrow=open;"
               "endFill=0;strokeColor=" + LINE + ";fontColor=#333333;fontSize=10;"
               "exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=1;entryY=0.5;"
               "entryDx=0;entryDy=0;")


def norm_sub(item):
    """子用例项：字符串默认为 include；(名称, 关系) 可指定 include / extend"""
    if isinstance(item, (tuple, list)):
        return item[0], item[1]
    return item, "include"


def q(s):
    return escape(s, {'"': "&quot;"})


class Cells:
    """收集 mxCell 元素"""

    def __init__(self):
        self._n = 1
        self.parts = []

    def _id(self, prefix="c"):
        self._n += 1
        return "%s%d" % (prefix, self._n)

    def vertex(self, cid, parent, value, style, x, y, w, h):
        self.parts.append(
            '        <mxCell id="%s" value="%s" style="%s" vertex="1" parent="%s">\n'
            '          <mxGeometry x="%.1f" y="%.1f" width="%.1f" height="%.1f" as="geometry" />\n'
            '        </mxCell>' % (cid, q(value), q(style), parent, x, y, w, h))
        return cid

    def edge(self, parent, value, style, src, tgt):
        cid = self._id("e")
        self.parts.append(
            '        <mxCell id="%s" value="%s" style="%s" edge="1" parent="%s" '
            'source="%s" target="%s">\n'
            '          <mxGeometry relative="1" as="geometry" />\n'
            '        </mxCell>' % (cid, q(value), q(style), parent, src, tgt))
        return cid

    def dump(self):
        return "\n".join(self.parts)


def write_drawio(filename, cells, page_w, page_h):
    xml = (
        '<mxfile host="app.diagrams.net" modified="2026-09-21T17:00:00.000Z" '
        'agent="WorkBuddy" version="24.7.17" type="device">\n'
        '  <diagram id="page-1" name="Page-1">\n'
        '    <mxGraphModel dx="1422" dy="798" grid="1" gridSize="10" guides="1" tooltips="1" '
        'connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="%d" pageHeight="%d" '
        'math="0" shadow="0">\n'
        '      <root>\n'
        '        <mxCell id="0" />\n'
        '        <mxCell id="1" parent="0" />\n'
        '%s\n'
        '      </root>\n'
        '    </mxGraphModel>\n'
        '  </diagram>\n'
        '</mxfile>\n' % (page_w, page_h, cells.dump())
    )
    path = os.path.join(OUT_DIR, filename)
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)
    print("[OK] %s  (%d x %d)" % (path, page_w, page_h))


# ================================================================ 树形模块图
TOP_W, TOP_H = 240, 60          # 顶层节点
MOD_H = 48                      # 模块节点高
MOD_MIN_W = 106                 # 模块最小宽
LEAF_W = 26                     # 叶子宽（竖排文字）
LEAF_GAP = 6                    # 叶子间距
MOD_GAP = 18                    # 模块间距
LEAF_LINE_H = 14                # 竖排文字每行高
LEAF_PAD = 12                   # 叶子上下内边距
GAP_LEVEL1 = 62                 # 顶层 → 模块
GAP_LEVEL2 = 62                 # 模块 → 叶子
MARGIN = 40


def leaf_text(text):
    """把文字拆成竖排（每字一行）"""
    return "<br>".join(list(text))


def build_module_tree(filename, system_name, module_label, modules):
    n = len(modules)
    mod_w = [max(MOD_MIN_W, len(items) * (LEAF_W + LEAF_GAP) - LEAF_GAP)
             for _, items in modules]
    total_w = sum(mod_w) + MOD_GAP * (n - 1)
    page_w = int(total_w + MARGIN * 2)

    top_y = 44
    mod_y = top_y + TOP_H + GAP_LEVEL1
    leaf_y = mod_y + MOD_H + GAP_LEVEL2
    leaf_h = [max(len(it) for it in items) * LEAF_LINE_H + LEAF_PAD for _, items in modules]
    page_h = int(leaf_y + max(leaf_h) + MARGIN)

    cs = Cells()
    # 顶层节点（系统名 + 模块归属，两行居中，本身即图题）
    top_x = MARGIN + (total_w - TOP_W) / 2.0
    cs.vertex("top", "1", "%s<br>%s" % (system_name, module_label), TOP_STYLE,
              top_x, top_y, TOP_W, TOP_H)

    x = MARGIN
    for i, (name, items) in enumerate(modules):
        w = mod_w[i]
        box_w = max(MOD_MIN_W, min(w, len(name) * 12 + 14))
        mid = cs.vertex("m%d" % i, "1", name, MOD_STYLE,
                        x + (w - box_w) / 2.0, mod_y, box_w, MOD_H)
        cs.edge("1", "", TREE_EDGE, "top", mid)

        # 叶子：竖排文字，顶部对齐
        row_w = len(items) * (LEAF_W + LEAF_GAP) - LEAF_GAP
        lx = x + (w - row_w) / 2.0
        for k, item in enumerate(items):
            lid = cs.vertex("m%dl%d" % (i, k), "1", leaf_text(item), LEAF_STYLE,
                            lx + k * (LEAF_W + LEAF_GAP), leaf_y,
                            LEAF_W, len(item) * LEAF_LINE_H + LEAF_PAD)
            cs.edge("1", "", TREE_EDGE, mid, lid)
        x += w + MOD_GAP

    write_drawio(filename, cs, page_w, page_h)


# ================================================================ 分层用例图
USE_W, USE_H = 172, 42          # 用例椭圆
ROW_GAP = 6                     # 子用例行距
GROUP_GAP = 26                  # 主用例组间距
ACTOR_W, ACTOR_H = 56, 88
MAIN_X = 220                    # 主用例列
COL_GAP = 130                   # 主用例列 → 子用例列


def build_usecase_diagram(filename, title, actors, groups):
    """
    actors: {key: label}
    groups: [(主用例名, [子用例名...], actor_key[, 关系类型])]
            关系类型 "include" / "extend"，缺省 include
    """
    SUB_X = MAIN_X + USE_W + COL_GAP
    top = 74
    y = top
    rows = []
    for g in groups:
        main, subs, akey = g[0], g[1], g[2]
        cnt = max(1, len(subs))
        gh = cnt * USE_H + (cnt - 1) * ROW_GAP
        main_y = y + gh / 2.0 - USE_H / 2.0
        sub_list = [(s, y + k * (USE_H + ROW_GAP)) for k, s in enumerate(subs)]
        rows.append((main, main_y, sub_list, akey))
        y += gh + GROUP_GAP
    total_h = y - GROUP_GAP
    page_h = int(top + total_h + 60)
    page_w = SUB_X + USE_W + 60

    cs = Cells()
    cs.vertex("title", "1", title, TITLE_STYLE, 0, 26, page_w, 24)

    # 角色位置：对齐其关联用例组的垂直中心
    ay = defaultdict(list)
    for main, main_y, subs, akey in rows:
        ay[akey].append(main_y + USE_H / 2.0)
    actor_pos = {}
    for k, ys in ay.items():
        cy = (min(ys) + max(ys)) / 2.0
        actor_pos[k] = (36, top + cy - ACTOR_H / 2.0)

    for aid, label in actors.items():
        x, yy = actor_pos[aid]
        cs.vertex(aid, "1", label, ACTOR_STYLE, x, yy, ACTOR_W, ACTOR_H)

    # 主用例 + 子用例 + 连线
    for idx, (main, main_y, sub_list, akey) in enumerate(rows):
        mid = cs.vertex("u%d" % idx, "1", main, UC_STYLE, MAIN_X, main_y, USE_W, USE_H)

        # 角色 → 主用例（起点按组序分散，避免全部汇聚一点）
        seq = ay[akey].index(main_y + USE_H / 2.0)
        frac = 0.5 if len(ay[akey]) == 1 else 0.10 + 0.80 * (seq / (len(ay[akey]) - 1.0))
        cs.edge("1", "", ASSOC_EDGE.format(frac=frac), akey, mid)

        for k, (sub, sy) in enumerate(sub_list):
            sub_name, rel = norm_sub(sub)
            sid = cs.vertex("u%ds%d" % (idx, k), "1", sub_name, UC_STYLE, SUB_X, sy, USE_W, USE_H)
            if rel == "extend":
                cs.edge("1", "«extend»", EXTEND_EDGE, sid, mid)
            else:
                cs.edge("1", "«include»", INCLUDE_EDGE, mid, sid)

    write_drawio(filename, cs, page_w, page_h)


# ================================================================ 数据定义
FRONT = [
    ("用户认证模块", ["用户注册", "用户登录", "密码找回", "Token刷新", "退出登录"]),
    ("酒店首页模块", ["酒店信息展示", "图片轮播", "公告展示"]),
    ("房型浏览模块", ["日期查询", "条件筛选", "价格/销量排序", "房型列表", "房型详情", "评论查看"]),
    ("在线预订模块", ["选择日期房数", "价格实时计算", "选择入住人", "提交订单", "模拟支付"]),
    ("订单管理模块", ["订单列表", "订单详情", "取消订单", "继续支付", "再次预订"]),
    ("入住记录模块", ["在住状态查看", "历史入住记录"]),
    ("我的收藏模块", ["收藏/取消收藏", "收藏列表"]),
    ("评价评论模块", ["发表评价", "我的评价"]),
    ("常用入住人模块", ["入住人列表", "新增/编辑/删除", "设为默认"]),
    ("个人中心模块", ["资料修改", "密码修改", "订单快捷入口"]),
]

BACK = [
    ("管理员登录模块", ["管理员登录", "管理员退出"]),
    ("数据仪表盘模块", ["核心指标统计", "入住率统计", "订单/营收趋势", "房型预订占比"]),
    ("用户管理模块", ["用户列表查询", "用户详情", "启用/禁用用户"]),
    ("酒店信息管理模块", ["酒店信息编辑", "图片管理"]),
    ("房型管理模块", ["房型列表", "新增房型", "编辑房型", "启用/停用房型"]),
    ("房间管理模块", ["房间列表", "新增/编辑/删除", "房间状态管理", "房态概览图"]),
    ("订单管理模块", ["订单列表查询", "订单详情", "取消订单"]),
    ("入住退住管理模块", ["待入住列表", "办理入住", "在住列表", "办理退住", "入住记录查询"]),
    ("评论管理模块", ["评论列表", "评论审核", "回复评论"]),
    ("支付退款管理模块", ["退款列表", "确认退款", "退款详情"]),
    ("公告管理模块", ["公告列表", "发布公告", "编辑/删除", "公告上下架"]),
]

# 管理员用例：主用例 / 子用例 / 角色（extend 单独标注）
ADMIN_GROUPS = [
    ("登录管理后台", [], "a1"),
    ("数据仪表盘", ["核心指标统计", "经营趋势图表"], "a1"),
    ("用户管理", ["查询用户信息", "启用/禁用用户"], "a1"),
    ("房型房间管理", ["房型维护", "房间维护", "房态概览"], "a1"),
    ("订单管理", ["查询订单", ("取消订单", "extend")], "a1"),
    ("入住退住管理", ["办理入住", "办理退住", ("结算额外费用", "extend")], "a1"),
    ("评论公告管理", ["审核评论", "回复评论", "发布公告"], "a1"),
    ("支付退款管理", ["确认退款", "查看退款详情"], "a1"),
    ("管理员账号管理", ["新增管理员账号"], "a2"),
]

# 用户用例
USER_GROUPS = [
    ("登录注册", ["用户注册", "密码找回"], "a1"),
    ("浏览酒店", ["查看酒店信息", "查看通知公告"], "a1"),
    ("搜索房型", ["查看房型详情", "查看房型评价"], "a1"),
    ("在线预订", ["选择入住人", "在线支付"], "a1"),
    ("订单管理", ["查看订单详情", "取消订单"], "a1"),
    ("我的收藏", ["收藏/取消收藏房型"], "a1"),
    ("常用入住人", ["新增/编辑/删除入住人"], "a1"),
    ("入住与评价", ["查看入住记录", "发表评价"], "a1"),
    ("个人中心", ["修改资料", "修改密码"], "a1"),
]


def main():
    build_module_tree("01-前台模块图.drawio", "酒店预约管理系统", "前台模块", FRONT)
    build_module_tree("02-后台模块图.drawio", "酒店预约管理系统", "后台模块", BACK)

    build_usecase_diagram("03-管理员用例图.drawio", "酒店预约管理系统 · 管理员用例图",
                          {"a1": "管理员", "a2": "超级管理员"}, ADMIN_GROUPS)

    build_usecase_diagram("04-用户用例图.drawio", "酒店预约管理系统 · 用户用例图",
                          {"a1": "普通用户"}, USER_GROUPS)


if __name__ == "__main__":
    main()
