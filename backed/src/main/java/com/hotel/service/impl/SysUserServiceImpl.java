package com.hotel.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hotel.entity.SysUser;
import com.hotel.mapper.SysUserMapper;
import com.hotel.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
