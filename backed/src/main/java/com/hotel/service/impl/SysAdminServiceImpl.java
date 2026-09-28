package com.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hotel.entity.SysAdmin;
import com.hotel.mapper.SysAdminMapper;
import com.hotel.service.SysAdminService;
import org.springframework.stereotype.Service;

@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements SysAdminService {
}
