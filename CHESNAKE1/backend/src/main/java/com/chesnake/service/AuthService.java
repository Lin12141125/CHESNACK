package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.config.RabbitConfig;
import com.chesnake.dto.LoginDTO;
import com.chesnake.dto.RegisterDTO;
import com.chesnake.entity.User;
import com.chesnake.mapper.UserMapper;
import com.chesnake.mq.NotifyMessage;
import com.chesnake.security.JwtUtil;
import com.chesnake.security.LoginUser;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.email()));
        if (count != null && count > 0) {
            throw new BusinessException("该邮箱已注册，请直接登录");
        }
        User user = new User();
        user.setEmail(dto.email());
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        // 安全加固：注册一律为普通用户，忽略前端传入的 role，防止注册提权；
        // 管理员账号只能由数据库预置或管理员后台创建
        user.setRole(2);
        userMapper.insert(user);

        // 异步发送欢迎通知（RabbitMQ 解耦）
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.NOTIFY_ROUTING_KEY,
                new NotifyMessage(dto.email(), "欢迎加入 CHESNAKE",
                        "你好，" + dto.username() + "！注册成功，开启你的零食之旅吧～"));
    }

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.email()));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        // 登录身份与注册身份需匹配
        if (dto.role() != null && !dto.role().equals(user.getRole())) {
            throw new BusinessException("身份选择不正确");
        }
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generate(user.getEmail(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("email", user.getEmail());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return data;
    }

    public LoginUser me() {
        return SecurityUtil.current();
    }
}
