package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 房间新增/更新请求
 */
@Data
public class RoomDTO implements Serializable {

    private Long id;
    private Long roomTypeId;
    private String roomNo;
    private String floor;
    private Integer status;
    private String remark;
}
