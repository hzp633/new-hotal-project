package com.hotel.controller;

import com.hotel.common.Result;
import com.hotel.dto.DashboardVO;
import com.hotel.entity.CheckInRecord;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.service.CheckInRecordService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomService;
import com.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * B 端控制台概览
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final CheckInRecordService checkInRecordService;
    private final RoomTypeService roomTypeService;

    /** 概览汇总 */
    @GetMapping("/summary")
    public Result<DashboardVO> summary() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        // 1. 今日下单数
        long todayOrders = reservationService.lambdaQuery()
                .ge(Reservation::getCreateTime, startOfDay)
                .lt(Reservation::getCreateTime, startOfNextDay)
                .count();

        // 2. 今日入住数（已支付或已入住）
        long todayCheckIns = reservationService.lambdaQuery()
                .eq(Reservation::getCheckInDate, today)
                .in(Reservation::getStatus, Arrays.asList(1, 2))
                .count();

        // 3. 当前在住数
        long activeCheckIns = checkInRecordService.lambdaQuery()
                .eq(CheckInRecord::getStatus, 1)
                .count();

        // 4. 本月营收（状态为已支付/已入住/已完成/已退款，且本月支付）
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        List<Reservation> monthReservations = reservationService.lambdaQuery()
                .in(Reservation::getStatus, Arrays.asList(1, 2, 3, 5))
                .ge(Reservation::getPayTime, startOfMonth)
                .lt(Reservation::getPayTime, startOfNextMonth)
                .list();
        BigDecimal monthRevenue = monthReservations.stream()
                .map(r -> r.getActualAmount() == null ? BigDecimal.ZERO : r.getActualAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 入住率
        long totalRooms = roomService.count();
        long occupiedRooms = roomService.lambdaQuery()
                .eq(Room::getStatus, 1)
                .count();
        double occupancyRate = totalRooms == 0 ? 0.0 : (occupiedRooms * 100.0 / totalRooms);

        // 6. 近 7 日订单趋势
        List<DashboardVO.DayTrend> orderTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            long count = reservationService.lambdaQuery()
                    .ge(Reservation::getCreateTime, dayStart)
                    .lt(Reservation::getCreateTime, dayEnd)
                    .count();
            orderTrend.add(DashboardVO.DayTrend.builder()
                    .date(date.toString())
                    .count(count)
                    .build());
        }

        // 7. 近 7 日营收趋势
        List<DashboardVO.DayTrend> revenueTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            List<Reservation> dayReservations = reservationService.lambdaQuery()
                    .ge(Reservation::getPayTime, dayStart)
                    .lt(Reservation::getPayTime, dayEnd)
                    .list();
            BigDecimal amount = dayReservations.stream()
                    .map(r -> r.getActualAmount() == null ? BigDecimal.ZERO : r.getActualAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueTrend.add(DashboardVO.DayTrend.builder()
                    .date(date.toString())
                    .amount(amount)
                    .build());
        }

        // 8. 房型订单占比
        List<Reservation> validReservations = reservationService.lambdaQuery()
                .in(Reservation::getStatus, Arrays.asList(1, 2, 3, 5))
                .list();
        Map<Long, Long> countByType = validReservations.stream()
                .filter(r -> r.getRoomTypeId() != null)
                .collect(Collectors.groupingBy(Reservation::getRoomTypeId, Collectors.counting()));
        List<RoomType> roomTypes = roomTypeService.list();
        List<DashboardVO.TypeProportion> typeProportion = roomTypes.stream()
                .map(rt -> DashboardVO.TypeProportion.builder()
                        .name(rt.getName())
                        .count(countByType.getOrDefault(rt.getId(), 0L))
                        .build())
                .collect(Collectors.toList());

        DashboardVO vo = DashboardVO.builder()
                .todayOrders(todayOrders)
                .todayCheckIns(todayCheckIns)
                .activeCheckIns(activeCheckIns)
                .monthRevenue(monthRevenue)
                .occupancyRate(occupancyRate)
                .orderTrend(orderTrend)
                .revenueTrend(revenueTrend)
                .typeProportion(typeProportion)
                .build();
        return Result.ok(vo);
    }
}
