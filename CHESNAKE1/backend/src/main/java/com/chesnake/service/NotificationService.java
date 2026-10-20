package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.entity.Notification;
import com.chesnake.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public Page<Notification> list(String email, long page, long size) {
        return notificationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getEmail, email)
                        .orderByDesc(Notification::getCreateTime));
    }

    public long unreadCount(String email) {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getEmail, email).eq(Notification::getIsRead, 0));
        return count == null ? 0 : count;
    }

    public void markRead(String email, Long id) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id).eq(Notification::getEmail, email)
                .set(Notification::getIsRead, 1));
    }
}
