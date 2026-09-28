package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 退款请求
 */
@Data
public class RefundDTO implements Serializable {

    private String reason;
}
