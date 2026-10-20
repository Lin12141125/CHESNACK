package com.chesnake.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.R;
import com.chesnake.entity.Notification;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public R<Page<Notification>> list(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size) {
        return R.ok(notificationService.list(SecurityUtil.currentEmail(), page, size));
    }

    @GetMapping("/unread")
    public R<Long> unread() {
        return R.ok(notificationService.unreadCount(SecurityUtil.currentEmail()));
    }

    @PutMapping("/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        notificationService.markRead(SecurityUtil.currentEmail(), id);
        return R.ok();
    }
}
