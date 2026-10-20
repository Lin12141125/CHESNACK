package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.config.RabbitConfig;
import com.chesnake.dto.CommunityCommentDTO;
import com.chesnake.entity.Community;
import com.chesnake.entity.CommunityComment;
import com.chesnake.mapper.CommunityCommentMapper;
import com.chesnake.mapper.CommunityMapper;
import com.chesnake.mq.CommunityEventMessage;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityCommentMapper communityCommentMapper;
    private final CommunityMapper communityMapper;
    private final RabbitTemplate rabbitTemplate;

    public List<CommunityComment> list(Long communityId) {
        List<CommunityComment> records = communityCommentMapper.selectList(
                new LambdaQueryWrapper<CommunityComment>()
                        .eq(CommunityComment::getCommunityId, communityId)
                        .orderByDesc(CommunityComment::getSendTime));
        List<CommunityComment> parents = new ArrayList<>();
        Map<Long, CommunityComment> map = new HashMap<>();
        for (CommunityComment c : records) {
            if (c.getPid() == null) {
                parents.add(c);
                map.put(c.getId(), c);
            }
        }
        for (CommunityComment c : records) {
            if (c.getPid() == null) continue;
            CommunityComment parent = map.get(c.getPid());
            if (parent != null) {
                if (parent.getReplies() == null) parent.setReplies(new ArrayList<>());
                parent.getReplies().add(c);
            }
        }
        return parents;
    }

    public void add(CommunityCommentDTO dto) {
        Community post = communityMapper.selectById(dto.communityId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        String username = SecurityUtil.currentUsername();

        CommunityComment comment = new CommunityComment();
        comment.setUsername(username);
        comment.setContent(dto.content());
        comment.setCommunityId(dto.communityId());
        comment.setPid(dto.pid() == null || dto.pid() <= 0 ? null : dto.pid());
        communityCommentMapper.insert(comment);

        // 异步通知帖子作者（RabbitMQ 解耦；toEmail 为评论者，避免自评自通知）
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.COMMUNITY_ROUTING_KEY,
                new CommunityEventMessage(CommunityEventMessage.TYPE_COMMENT, dto.communityId(),
                        comment.getId(), username, SecurityUtil.currentEmail(), dto.content()));
    }
}
