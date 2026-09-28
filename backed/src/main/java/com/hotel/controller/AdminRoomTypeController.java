package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.RoomTypeDTO;
import com.hotel.entity.RoomType;
import com.hotel.service.RoomTypeService;
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
 * B 端房型管理。
 */
@RestController
@RequestMapping("/admin/room-type")
@RequiredArgsConstructor
public class AdminRoomTypeController {

    private final RoomTypeService roomTypeService;

    /** 分页查询房型 */
    @GetMapping("/page")
    public Result<PageResult<RoomType>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<RoomType> page = new Page<>(current, size);
        Page<RoomType> result = roomTypeService.lambdaQuery()
                .like(name != null && !name.isEmpty(), RoomType::getName, name)
                .eq(status != null, RoomType::getStatus, status)
                .orderByDesc(RoomType::getCreateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 新增房型（hotelId 固定为 1，status 默认启用） */
    @PostMapping
    public Result<RoomType> create(@RequestBody RoomTypeDTO dto) {
        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(dto, roomType);
        roomType.setHotelId(1L);
        if (roomType.getStatus() == null) {
            roomType.setStatus(1);
        }
        roomTypeService.save(roomType);
        return Result.ok(roomType);
    }

    /** 更新房型 */
    @PutMapping
    public Result<RoomType> update(@RequestBody RoomTypeDTO dto) {
        if (dto.getId() == null || roomTypeService.getById(dto.getId()) == null) {
            throw new BusinessException(ResultCode.ROOM_TYPE_NOT_FOUND);
        }
        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(dto, roomType);
        roomTypeService.updateById(roomType);
        return Result.ok(roomTypeService.getById(dto.getId()));
    }

    /** 切换房型状态 */
    @PutMapping("/{id}/status")
    public Result<RoomType> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        RoomType roomType = roomTypeService.getById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.ROOM_TYPE_NOT_FOUND);
        }
        RoomType update = new RoomType();
        update.setId(id);
        update.setStatus(status);
        roomTypeService.updateById(update);
        return Result.ok(roomTypeService.getById(id));
    }

    /** 删除房型 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (roomTypeService.getById(id) == null) {
            throw new BusinessException(ResultCode.ROOM_TYPE_NOT_FOUND);
        }
        roomTypeService.removeById(id);
        return Result.ok();
    }
}
