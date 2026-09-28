package com.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hotel.entity.Guest;
import com.hotel.mapper.GuestMapper;
import com.hotel.service.GuestService;
import org.springframework.stereotype.Service;

@Service
public class GuestServiceImpl extends ServiceImpl<GuestMapper, Guest> implements GuestService {
}
