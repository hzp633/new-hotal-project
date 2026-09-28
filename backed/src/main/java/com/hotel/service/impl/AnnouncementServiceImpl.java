package com.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hotel.entity.Announcement;
import com.hotel.mapper.AnnouncementMapper;
import com.hotel.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}
