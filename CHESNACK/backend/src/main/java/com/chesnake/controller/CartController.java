package com.chesnake.controller;

import com.chesnake.common.R;
import com.chesnake.dto.CartAddDTO;
import com.chesnake.entity.CartVO;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public R<List<CartVO>> list() {
        return R.ok(cartService.list(SecurityUtil.currentEmail()));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.add(SecurityUtil.currentEmail(), dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> updateAmount(@PathVariable Long id, @RequestParam int amount) {
        cartService.updateAmount(SecurityUtil.currentEmail(), id, amount);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        cartService.delete(SecurityUtil.currentEmail(), id);
        return R.ok();
    }
}
