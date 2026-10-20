package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.CommentAddDTO;
import com.chesnake.dto.CommentReplyDTO;
import com.chesnake.entity.Comment;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 商品评价列表（含回复树） */
    @GetMapping("/goods/{goodsId}")
    public R<List<Comment>> list(@PathVariable Long goodsId, @RequestParam(required = false) Integer level) {
        return R.ok(commentService.listByGoods(goodsId, level));
    }

    /** 星级分布 */
    @GetMapping("/stats/{goodsId}")
    public R<List<Map<String, Object>>> stats(@PathVariable Long goodsId) {
        return R.ok(commentService.countStats(goodsId));
    }

    /** 提交订单评价 */
    @PostMapping
    public R<Void> add(@Valid @RequestBody CommentAddDTO dto) {
        commentService.add(SecurityUtil.currentUsername(), dto);
        return R.ok();
    }

    /** 回复评价 */
    @PostMapping("/reply")
    public R<Void> reply(@Valid @RequestBody CommentReplyDTO dto) {
        commentService.reply(SecurityUtil.currentUsername(), dto);
        return R.ok();
    }

    /** 举报评价 */
    @PostMapping("/report/{id}")
    public R<Void> report(@PathVariable Long id) {
        commentService.report(id);
        return R.ok();
    }
}
