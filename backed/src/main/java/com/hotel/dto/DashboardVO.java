package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 控制台概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO implements Serializable {

    private Long todayOrders;
    private Long todayCheckIns;
    private Long activeCheckIns;
    private BigDecimal monthRevenue;
    private Double occupancyRate;
    private List<DayTrend> orderTrend;
    private List<DayTrend> revenueTrend;
    private List<TypeProportion> typeProportion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTrend implements Serializable {
        private String date;
        private Long count;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeProportion implements Serializable {
        private String name;
        private Long count;
    }
}
