package com.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hotel.entity.CheckInRecord;
import com.hotel.mapper.CheckInRecordMapper;
import com.hotel.service.CheckInRecordService;
import org.springframework.stereotype.Service;

@Service
public class CheckInRecordServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord> implements CheckInRecordService {
}
