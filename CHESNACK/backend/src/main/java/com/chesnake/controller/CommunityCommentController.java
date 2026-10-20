package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.CommunityCommentDTO;
import com.chesnake.entity.CommunityComment;
import com.chesnake.service.CommunityCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    @GetMapping("/{id}/comments")
    public R<List<CommunityComment>> list(@PathVariable Long id) {
        return R.ok(communityCommentService.list(id));
    }

    @PostMapping("/comment")
    public R<Void> add(@Valid @RequestBody CommunityCommentDTO dto) {
        communityCommentService.add(dto);
        return R.ok();
    }
}
