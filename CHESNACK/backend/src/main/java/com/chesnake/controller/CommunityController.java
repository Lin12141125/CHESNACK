package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.PostDTO;
import com.chesnake.entity.Community;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /** 帖子列表：mode = personal(我的) / recommend(推荐) / follow(关注) */
    @GetMapping
    public R<List<Community>> list(@RequestParam(defaultValue = "recommend") String mode) {
        return switch (mode) {
            case "personal" -> R.ok(communityService.listPersonal(SecurityUtil.currentEmail()));
            case "follow" -> R.ok(communityService.listFollow(SecurityUtil.currentUsername()));
            default -> R.ok(communityService.listRecommend());
        };
    }

    @GetMapping("/{id}")
    public R<Community> detail(@PathVariable Long id) {
        return R.ok(communityService.detail(id));
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Community> create(@RequestParam String title,
                               @RequestParam(required = false) String content,
                               @RequestPart(value = "file", required = false) MultipartFile file) {
        return R.ok(communityService.create(new PostDTO(title, content), file));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        communityService.delete(id);
        return R.ok();
    }
}
