package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 办理退住请求
 */
@Data
public class CheckOutDTO implements Serializable {

    private BigDecimal extraFee;
    private String remark;
}
