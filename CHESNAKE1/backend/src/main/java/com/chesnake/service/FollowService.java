package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.dto.FollowDTO;
import com.chesnake.entity.Follow;
import com.chesnake.entity.User;
import com.chesnake.mapper.FollowMapper;
import com.chesnake.mapper.UserMapper;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    public void follow(String currentUsername, FollowDTO dto) {
        if (currentUsername.equals(dto.username())) {
            throw new BusinessException("不能关注自己");
        }
        User target = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.username()));
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        Long count = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getAccount, target.getEmail())
                .eq(Follow::getFollowee, currentUsername));
        if (count != null && count > 0) {
            throw new BusinessException("已关注该用户");
        }
        Follow follow = new Follow();
        follow.setAccount(target.getEmail());
        follow.setFollowee(currentUsername);
        follow.setFolloweeEmail(SecurityUtil.currentEmail());
        followMapper.insert(follow);
    }

    public void unfollow(String currentUsername, String targetUsername) {
        User target = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, targetUsername));
        if (target == null) return;
        followMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getAccount, target.getEmail())
                .eq(Follow::getFollowee, currentUsername));
    }

    /** 我关注的人（用户列表） */
    public List<User> listFollowees(String currentUsername) {
        List<String> accounts = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowee, currentUsername))
                .stream().map(Follow::getAccount).toList();
        if (accounts.isEmpty()) return List.of();
        return userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getEmail, accounts));
    }
}
