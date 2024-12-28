package com.epoch.mrs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epoch.mrs.domain.po.User;
import com.epoch.mrs.mapper.UserMapper;
import com.epoch.mrs.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
