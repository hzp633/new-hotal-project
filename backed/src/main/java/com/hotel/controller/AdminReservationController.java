package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.AdminReservationQueryDTO;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.security.BaseContext;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * B 端订单管理
 */
@RestController
@RequestMapping("/admin/reservation")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    /** 订单分页查询 */
    @GetMapping("/page")
    public Result<PageResult<Reservation>> page(AdminReservationQueryDTO dto) {
        Page<Reservation> page = new Page<>(dto.getCurrent(), dto.getSize());
        Page<Reservation> result = reservationService.lambdaQuery()
                .like(StringUtils.hasText(dto.getOrderNo()), Reservation::getOrderNo, dto.getOrderNo())
                .eq(dto.getStatus() != null, Reservation::getStatus, dto.getStatus())
                .like(StringUtils.hasText(dto.getPhone()), Reservation::getContactPhone, dto.getPhone())
                .ge(dto.getStartDate() != null, Reservation::getCheckInDate, dto.getStartDate())
                .le(dto.getEndDate() != null, Reservation::getCheckInDate, dto.getEndDate())
                .orderByDesc(Reservation::getCreateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<Reservation> detail(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        return Result.ok(reservation);
    }

    /** 管理员取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        Integer status = reservation.getStatus();
        if (status != null && status == 0) {
            reservation.setStatus(4);
            reservation.setCancelTime(now);
            reservationService.updateById(reservation);
        } else if (status != null && status == 1) {
            reservation.setStatus(5);
            reservation.setCancelTime(now);
            reservationService.updateById(reservation);
            Payment payment = paymentService.lambdaQuery()
                    .eq(Payment::getReservationId, id)
                    .eq(Payment::getStatus, 1)
                    .one();
            if (payment != null) {
                payment.setStatus(3);
                payment.setRefundAmount(payment.getAmount());
                payment.setRefundTime(now);
                payment.setRefundReason("管理员取消");
                payment.setRefundOperator(BaseContext.getAdminId());
                paymentService.updateById(payment);
            }
        } else {
            throw new BusinessException(ResultCode.RESERVATION_STATUS_ERROR);
        }
        return Result.ok();
    }
}
