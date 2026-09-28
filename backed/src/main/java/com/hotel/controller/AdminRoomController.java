package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.RoomDTO;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
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
 * B 端房间管理。
 */
@RestController
@RequestMapping("/admin/room")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;

    /** 分页查询房间 */
    @GetMapping("/page")
    public Result<PageResult<Room>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String floor) {
        Page<Room> page = new Page<>(current, size);
        Page<Room> result = roomService.lambdaQuery()
                .eq(roomTypeId != null, Room::getRoomTypeId, roomTypeId)
                .eq(status != null, Room::getStatus, status)
                .eq(floor != null && !floor.isEmpty(), Room::getFloor, floor)
                .orderByDesc(Room::getCreateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 新增房间（hotelId 固定为 1，status 默认空闲） */
    @PostMapping
    public Result<Room> create(@RequestBody RoomDTO dto) {
        Room room = new Room();
        BeanUtils.copyProperties(dto, room);
        room.setHotelId(1L);
        if (room.getStatus() == null) {
            room.setStatus(0);
        }
        roomService.save(room);
        return Result.ok(room);
    }

    /** 更新房间 */
    @PutMapping
    public Result<Room> update(@RequestBody RoomDTO dto) {
        if (dto.getId() == null || roomService.getById(dto.getId()) == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        Room room = new Room();
        BeanUtils.copyProperties(dto, room);
        roomService.updateById(room);
        return Result.ok(roomService.getById(dto.getId()));
    }

    /** 变更房间状态 */
    @PutMapping("/{id}/status")
    public Result<Room> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Room room = roomService.getById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        Room update = new Room();
        update.setId(id);
        update.setStatus(status);
        roomService.updateById(update);
        return Result.ok(roomService.getById(id));
    }

    /** 删除房间 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (roomService.getById(id) == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        roomService.removeById(id);
        return Result.ok();
    }
}
