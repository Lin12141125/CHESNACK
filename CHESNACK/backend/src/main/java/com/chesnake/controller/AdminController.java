package com.chesnake.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.R;
import com.chesnake.dto.GoodsSaveDTO;
import com.chesnake.entity.AdminLog;
import com.chesnake.entity.Comment;
import com.chesnake.entity.Goods;
import com.chesnake.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ================= 仓库管理 =================

    @PostMapping("/goods")
    public R<Goods> addGoods(@Valid @RequestBody GoodsSaveDTO dto) {
        return R.ok(adminService.saveGoods(dto));
    }

    @PutMapping("/goods")
    public R<Goods> updateGoods(@Valid @RequestBody GoodsSaveDTO dto) {
        if (dto.id() == null) {
            throw new com.chesnake.common.BusinessException("缺少商品ID");
        }
        return R.ok(adminService.saveGoods(dto));
    }

    @DeleteMapping("/goods/{id}")
    public R<Void> deleteGoods(@PathVariable Long id) {
        adminService.deleteGoods(id);
        return R.ok();
    }

    // ================= 举报管理 =================

    @GetMapping("/reports")
    public R<List<Comment>> reports() {
        return R.ok(adminService.listReported());
    }

    @DeleteMapping("/reports/{id}")
    public R<Void> deleteReported(@PathVariable Long id) {
        adminService.deleteReportedComment(id);
        return R.ok();
    }

    // ================= 审计日志 =================

    @GetMapping("/logs")
    public R<Page<AdminLog>> logs(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "20") long size) {
        return R.ok(adminService.logs(page, size));
    }
}
