package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.dto.CartAddDTO;
import com.chesnake.entity.Cart;
import com.chesnake.entity.CartVO;
import com.chesnake.entity.Goods;
import com.chesnake.mapper.CartMapper;
import com.chesnake.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;

    public List<CartVO> list(String email) {
        return cartMapper.selectCartVO(email);
    }

    public void add(String email, CartAddDTO dto) {
        Goods goods = goodsMapper.selectById(dto.goodId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        Cart exist = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getEmail, email).eq(Cart::getGoodId, dto.goodId()));
        if (exist == null) {
            Cart cart = new Cart();
            cart.setEmail(email);
            cart.setGoodId(dto.goodId());
            cart.setAmount(dto.amount());
            cart.setPrice(goods.getPrice().multiply(BigDecimal.valueOf(dto.amount())));
            cartMapper.insert(cart);
        } else {
            int amount = exist.getAmount() + dto.amount();
            cartMapper.update(null, new LambdaUpdateWrapper<Cart>()
                    .eq(Cart::getId, exist.getId())
                    .set(Cart::getAmount, amount)
                    .set(Cart::getPrice, goods.getPrice().multiply(BigDecimal.valueOf(amount))));
        }
    }

    public void updateAmount(String email, Long cartId, int amount) {
        if (amount <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getEmail().equals(email)) {
            throw new BusinessException("购物车记录不存在");
        }
        Goods goods = goodsMapper.selectById(cart.getGoodId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        cartMapper.update(null, new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .set(Cart::getAmount, amount)
                .set(Cart::getPrice, goods.getPrice().multiply(BigDecimal.valueOf(amount))));
    }

    public void delete(String email, Long cartId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId).eq(Cart::getEmail, email));
    }
}
