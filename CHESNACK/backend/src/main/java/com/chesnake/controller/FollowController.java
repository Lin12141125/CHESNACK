package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.FollowDTO;
import com.chesnake.entity.User;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public R<Void> follow(@Valid @RequestBody FollowDTO dto) {
        followService.follow(SecurityUtil.currentUsername(), dto);
        return R.ok();
    }

    @DeleteMapping("/{username}")
    public R<Void> unfollow(@PathVariable String username) {
        followService.unfollow(SecurityUtil.currentUsername(), username);
        return R.ok();
    }

    @GetMapping
    public R<List<User>> list() {
        return R.ok(followService.listFollowees(SecurityUtil.currentUsername()));
    }
}
