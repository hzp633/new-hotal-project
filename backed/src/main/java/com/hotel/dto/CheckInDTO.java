package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 办理入住请求
 */
@Data
public class CheckInDTO implements Serializable {

    private Long reservationId;
    private Long roomId;
    private String remark;
}
