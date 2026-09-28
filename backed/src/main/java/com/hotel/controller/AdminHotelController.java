package com.hotel.controller;

import com.hotel.common.Result;
import com.hotel.dto.HotelUpdateDTO;
import com.hotel.entity.Hotel;
import com.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * B 端酒店信息管理（单酒店系统，固定 ID=1）。
 */
@RestController
@RequestMapping("/admin/hotel")
@RequiredArgsConstructor
public class AdminHotelController {

    private final HotelService hotelService;

    /** 更新酒店信息（ID 固定为 1） */
    @PutMapping
    public Result<Hotel> update(@RequestBody HotelUpdateDTO dto) {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        BeanUtils.copyProperties(dto, hotel);
        hotelService.updateById(hotel);
        return Result.ok(hotelService.getById(1L));
    }
}
