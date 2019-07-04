package com.mp.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mp.dao.UserMapper;
import com.mp.pojo.User;
import com.mp.service.UserServuce;

@Service
public class UserServuceImpl extends ServiceImpl<UserMapper, User> implements UserServuce {

}
