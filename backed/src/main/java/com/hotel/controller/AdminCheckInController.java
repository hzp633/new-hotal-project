package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.CheckInDTO;
import com.hotel.dto.CheckOutDTO;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.security.BaseContext;
import com.hotel.service.CheckInRecordService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * B 端入住 / 退住管理
 */
@RestController
@RequestMapping("/admin/checkin")
@RequiredArgsConstructor
public class AdminCheckInController {

    private final CheckInRecordService checkInRecordService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    /** 待入住订单（已支付且入住日期不早于今天） */
    @GetMapping("/pending")
    public Result<List<Reservation>> pending() {
        LocalDate today = LocalDate.now();
        List<Reservation> list = reservationService.lambdaQuery()
                .eq(Reservation::getStatus, 1)
                .ge(Reservation::getCheckInDate, today)
                .orderByAsc(Reservation::getCheckInDate)
                .list();
        return Result.ok(list);
    }

    /** 办理入住 */
    @PostMapping
    public Result<Void> checkIn(@RequestBody CheckInDTO dto) {
        Reservation reservation = reservationService.getById(dto.getReservationId());
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == null || reservation.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESERVATION_STATUS_ERROR);
        }
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        if (room.getStatus() == null || room.getStatus() != 0) {
            throw new BusinessException(ResultCode.ROOM_OCCUPIED);
        }
        room.setStatus(1);
        roomService.updateById(room);

        CheckInRecord record = new CheckInRecord();
        record.setReservationId(reservation.getId());
        record.setUserId(reservation.getUserId());
        record.setHotelId(reservation.getHotelId());
        record.setRoomId(dto.getRoomId());
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus(1);
        record.setOperatorId(BaseContext.getAdminId());
        record.setRemark(dto.getRemark());
        checkInRecordService.save(record);

        reservation.setStatus(2);
        reservationService.updateById(reservation);
        return Result.ok();
    }

    /** 在住记录 */
    @GetMapping("/active")
    public Result<List<CheckInRecord>> active() {
        List<CheckInRecord> list = checkInRecordService.lambdaQuery()
                .eq(CheckInRecord::getStatus, 1)
                .orderByDesc(CheckInRecord::getCheckInTime)
                .list();
        return Result.ok(list);
    }

    /** 办理退住 */
    @PostMapping("/{reservationId}/checkout")
    public Result<Void> checkout(@PathVariable Long reservationId, @RequestBody CheckOutDTO dto) {
        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == null || reservation.getStatus() != 2) {
            throw new BusinessException(ResultCode.NOT_CHECKED_IN);
        }
        CheckInRecord record = checkInRecordService.lambdaQuery()
                .eq(CheckInRecord::getReservationId, reservationId)
                .eq(CheckInRecord::getStatus, 1)
                .one();
        if (record == null) {
            throw new BusinessException(ResultCode.CHECK_IN_RECORD_NOT_FOUND);
        }
        record.setCheckOutTime(LocalDateTime.now());
        record.setExtraFee(dto.getExtraFee());
        record.setStatus(2);
        record.setRemark(dto.getRemark());
        checkInRecordService.updateById(record);

        Room room = roomService.getById(record.getRoomId());
        if (room != null) {
            room.setStatus(0);
            roomService.updateById(room);
        }

        reservation.setStatus(3);
        reservationService.updateById(reservation);
        return Result.ok();
    }

    /** 入住记录分页（已退住） */
    @GetMapping("/page")
    public Result<PageResult<CheckInRecord>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<CheckInRecord> page = new Page<>(current, size);
        Page<CheckInRecord> result = checkInRecordService.lambdaQuery()
                .eq(CheckInRecord::getStatus, 2)
                .orderByDesc(CheckInRecord::getCheckOutTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }
}
