package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.ReservationCreateDTO;
import com.hotel.dto.ReservationVO;
import com.hotel.entity.Payment;
import com.hotel.entity.Reservation;
import com.hotel.entity.RoomType;
import com.hotel.security.BaseContext;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C 端订单：创建 / 支付 / 查询 / 取消。
 */
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomTypeService roomTypeService;
    private final PaymentService paymentService;

    /** 创建订单 */
    @PostMapping
    public Result<ReservationVO> create(@RequestBody @Valid ReservationCreateDTO dto) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null
                || !dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new BusinessException(ResultCode.DATE_RANGE_INVALID);
        }
        int days = (int) ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());

        RoomType roomType = roomTypeService.getById(dto.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException(ResultCode.ROOM_TYPE_NOT_FOUND);
        }
        if (roomType.getStatus() == null || roomType.getStatus() != 1) {
            throw new BusinessException(ResultCode.ROOM_TYPE_DISABLED);
        }

        BigDecimal totalPrice = roomType.getPrice()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(dto.getRoomCount()));

        Reservation reservation = new Reservation();
        reservation.setOrderNo("HOTEL" + System.currentTimeMillis() + randomFourDigits());
        reservation.setUserId(BaseContext.getUserId());
        reservation.setHotelId(1L);
        reservation.setRoomTypeId(dto.getRoomTypeId());
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setDays(days);
        reservation.setRoomCount(dto.getRoomCount());
        reservation.setTotalPrice(totalPrice);
        reservation.setContactName(dto.getContactName());
        reservation.setContactPhone(dto.getContactPhone());
        reservation.setGuestInfo(dto.getGuestInfo());
        reservation.setRemark(dto.getRemark());
        reservation.setStatus(0);
        reservationService.save(reservation);

        return Result.ok(buildVO(reservation, roomType));
    }

    /** 支付订单 */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == null || reservation.getStatus() != 0) {
            throw new BusinessException(ResultCode.PAYMENT_ALREADY_PAID);
        }

        Payment payment = new Payment();
        payment.setReservationId(reservation.getId());
        payment.setUserId(reservation.getUserId());
        payment.setPayNo("PAY" + System.currentTimeMillis());
        payment.setAmount(reservation.getTotalPrice());
        payment.setPayMethod("alipay");
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentService.save(payment);

        LocalDateTime now = LocalDateTime.now();
        reservation.setStatus(1);
        reservation.setActualAmount(reservation.getTotalPrice());
        reservation.setPayTime(now);
        reservationService.updateById(reservation);
        return Result.ok();
    }

    /** 当前用户订单列表（按 createTime 倒序，可选状态过滤） */
    @GetMapping("/list")
    public Result<List<ReservationVO>> list(@RequestParam(required = false) Integer status) {
        Long userId = BaseContext.getUserId();
        var chain = reservationService.lambdaQuery()
                .eq(Reservation::getUserId, userId);
        if (status != null) {
            chain.eq(Reservation::getStatus, status);
        }
        chain.orderByDesc(Reservation::getCreateTime);
        List<Reservation> reservations = chain.list();
        if (reservations.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(buildVOList(reservations));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<ReservationVO> detail(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        RoomType roomType = roomTypeService.getById(reservation.getRoomTypeId());
        return Result.ok(buildVO(reservation, roomType));
    }

    /** 取消订单（待支付→已取消；已支付→已退款并退款） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        Integer status = reservation.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if (status != null && status == 0) {
            reservation.setStatus(4);
            reservation.setCancelTime(now);
        } else if (status != null && status == 1) {
            reservation.setStatus(5);
            reservation.setCancelTime(now);
            Payment payment = paymentService.lambdaQuery()
                    .eq(Payment::getReservationId, reservation.getId())
                    .eq(Payment::getStatus, 1)
                    .one();
            if (payment != null) {
                payment.setStatus(3);
                payment.setRefundAmount(payment.getAmount());
                payment.setRefundReason("用户取消");
                payment.setRefundTime(now);
                paymentService.updateById(payment);
            }
        } else {
            throw new BusinessException(ResultCode.RESERVATION_STATUS_ERROR);
        }
        reservationService.updateById(reservation);
        return Result.ok();
    }

    // ===== 内部构建 VO =====

    private ReservationVO buildVO(Reservation reservation, RoomType roomType) {
        if (roomType == null) {
            return ReservationVO.builder()
                    .id(reservation.getId())
                    .orderNo(reservation.getOrderNo())
                    .roomTypeId(reservation.getRoomTypeId())
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .days(reservation.getDays())
                    .roomCount(reservation.getRoomCount())
                    .totalPrice(reservation.getTotalPrice())
                    .actualAmount(reservation.getActualAmount())
                    .contactName(reservation.getContactName())
                    .contactPhone(reservation.getContactPhone())
                    .guestInfo(reservation.getGuestInfo())
                    .status(reservation.getStatus())
                    .payTime(reservation.getPayTime())
                    .cancelTime(reservation.getCancelTime())
                    .remark(reservation.getRemark())
                    .createTime(reservation.getCreateTime())
                    .build();
        }
        return ReservationVO.builder()
                .id(reservation.getId())
                .orderNo(reservation.getOrderNo())
                .roomTypeId(reservation.getRoomTypeId())
                .roomTypeName(roomType.getName())
                .roomTypePrice(roomType.getPrice())
                .roomTypeImages(roomType.getImages())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .days(reservation.getDays())
                .roomCount(reservation.getRoomCount())
                .totalPrice(reservation.getTotalPrice())
                .actualAmount(reservation.getActualAmount())
                .contactName(reservation.getContactName())
                .contactPhone(reservation.getContactPhone())
                .guestInfo(reservation.getGuestInfo())
                .status(reservation.getStatus())
                .payTime(reservation.getPayTime())
                .cancelTime(reservation.getCancelTime())
                .remark(reservation.getRemark())
                .createTime(reservation.getCreateTime())
                .build();
    }

    private List<ReservationVO> buildVOList(List<Reservation> reservations) {
        Set<Long> roomTypeIds = reservations.stream()
                .map(Reservation::getRoomTypeId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, RoomType> roomTypeMap;
        if (roomTypeIds.isEmpty()) {
            roomTypeMap = Collections.emptyMap();
        } else {
            List<RoomType> roomTypes = roomTypeService.listByIds(roomTypeIds);
            roomTypeMap = roomTypes.stream()
                    .collect(Collectors.toMap(RoomType::getId, Function.identity()));
        }
        return reservations.stream()
                .map(r -> buildVO(r, roomTypeMap.get(r.getRoomTypeId())))
                .collect(Collectors.toList());
    }

    private String randomFourDigits() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
