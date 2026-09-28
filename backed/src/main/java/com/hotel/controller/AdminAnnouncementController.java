package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.AnnouncementDTO;
import com.hotel.entity.Announcement;
import com.hotel.security.BaseContext;
import com.hotel.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * B 端公告管理。
 */
@RestController
@RequestMapping("/admin/announcement")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    /** 分页查询公告 */
    @GetMapping("/page")
    public Result<PageResult<Announcement>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        Page<Announcement> page = new Page<>(current, size);
        Page<Announcement> result = announcementService.lambdaQuery()
                .like(title != null && !title.isEmpty(), Announcement::getTitle, title)
                .eq(status != null, Announcement::getStatus, status)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 新增公告（publisherId 取当前登录管理员，isTop/status 默认值见需求） */
    @PostMapping
    public Result<Announcement> create(@RequestBody AnnouncementDTO dto) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(dto, announcement);
        announcement.setPublisherId(BaseContext.getAdminId());
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        announcementService.save(announcement);
        return Result.ok(announcement);
    }

    /** 更新公告 */
    @PutMapping
    public Result<Announcement> update(@RequestBody AnnouncementDTO dto) {
        if (dto.getId() == null || announcementService.getById(dto.getId()) == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(dto, announcement);
        announcementService.updateById(announcement);
        return Result.ok(announcementService.getById(dto.getId()));
    }

    /** 切换公告状态 */
    @PutMapping("/{id}/status")
    public Result<Announcement> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        Announcement update = new Announcement();
        update.setId(id);
        update.setStatus(status);
        announcementService.updateById(update);
        return Result.ok(announcementService.getById(id));
    }

    /** 删除公告 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (announcementService.getById(id) == null) {
            throw new BusinessException(ResultCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcementService.removeById(id);
        return Result.ok();
    }
}
