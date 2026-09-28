package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.RefundDTO;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.security.BaseContext;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * B 端支付 / 退款管理
 */
@RestController
@RequestMapping("/admin/payment")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    /** 退款记录分页（已支付 / 已退款） */
    @GetMapping("/refund/page")
    public Result<PageResult<Payment>> refundPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Payment> page = new Page<>(current, size);
        Page<Payment> result = paymentService.lambdaQuery()
                .in(Payment::getStatus, Arrays.asList(1, 3))
                .orderByDesc(Payment::getUpdateTime)
                .page(page);
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    /** 处理退款 */
    @PostMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id, @RequestBody RefundDTO dto) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        Integer status = payment.getStatus();
        if (status != null && status == 3) {
            throw new BusinessException(ResultCode.PAYMENT_REFUNDED);
        }
        if (status == null || status != 1) {
            throw new BusinessException(ResultCode.REFUND_NOT_ALLOWED);
        }
        LocalDateTime now = LocalDateTime.now();
        payment.setStatus(3);
        payment.setRefundAmount(payment.getAmount());
        payment.setRefundReason(dto.getReason());
        payment.setRefundTime(now);
        payment.setRefundOperator(BaseContext.getAdminId());
        paymentService.updateById(payment);

        Reservation reservation = reservationService.getById(payment.getReservationId());
        if (reservation != null && reservation.getStatus() != null && reservation.getStatus() == 1) {
            reservation.setStatus(5);
            reservation.setCancelTime(now);
            reservationService.updateById(reservation);
        }
        return Result.ok();
    }
}
