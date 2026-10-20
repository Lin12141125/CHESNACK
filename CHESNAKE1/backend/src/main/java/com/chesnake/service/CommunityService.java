package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.config.RabbitConfig;
import com.chesnake.dto.PostDTO;
import com.chesnake.entity.Community;
import com.chesnake.entity.Follow;
import com.chesnake.mapper.CommunityMapper;
import com.chesnake.mapper.FollowMapper;
import com.chesnake.mq.CommunityEventMessage;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;
    private final FollowMapper followMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${chesnake.upload-dir:uploads}")
    private String uploadDir;

    /** 发帖（可带图） */
    public Community create(PostDTO dto, MultipartFile file) {
        String account = SecurityUtil.currentEmail();
        String username = SecurityUtil.currentUsername();

        Community post = new Community();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setUsername(username);
        post.setAccount(account);
        if (file != null && !file.isEmpty()) {
            post.setImg1(saveImage(file));
        }
        communityMapper.insert(post);

        // 异步通知粉丝（RabbitMQ 解耦）
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.COMMUNITY_ROUTING_KEY,
                new CommunityEventMessage(CommunityEventMessage.TYPE_POST, post.getId(), null,
                        username, account, dto.title()));
        return post;
    }

    public List<Community> listPersonal(String account) {
        return communityMapper.selectList(new LambdaQueryWrapper<Community>()
                .eq(Community::getAccount, account).orderByDesc(Community::getSendTime));
    }

    public List<Community> listRecommend() {
        return communityMapper.selectList(new LambdaQueryWrapper<Community>()
                .orderByDesc(Community::getSendTime));
    }

    public List<Community> listFollow(String username) {
        List<String> accounts = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowee, username))
                .stream().map(Follow::getAccount).toList();
        if (accounts.isEmpty()) return List.of();
        return communityMapper.selectList(new LambdaQueryWrapper<Community>()
                .in(Community::getAccount, accounts).orderByDesc(Community::getSendTime));
    }

    public Community detail(Long id) {
        Community post = communityMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return post;
    }

    public void delete(Long id) {
        Community post = communityMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        String account = SecurityUtil.currentEmail();
        if (!account.equals(post.getAccount()) && !SecurityUtil.isAdmin()) {
            throw new BusinessException("只能删除自己的帖子");
        }
        communityMapper.deleteById(id);
    }

    private String saveImage(MultipartFile file) {
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            file.transferTo(new File(dir, filename).getAbsoluteFile());
        } catch (IOException e) {
            log.error("保存上传文件失败", e);
            throw new BusinessException("图片保存失败");
        }
        return "uploads/" + filename;
    }
}
