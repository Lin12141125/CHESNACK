package com.chesnake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chesnake.entity.Cart;
import com.chesnake.entity.CartVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CartMapper extends BaseMapper<Cart> {

    @Select("SELECT c.id, c.email, c.good_id, c.amount, c.price, g.name, g.price AS unit_price, g.img1 " +
            "FROM cart c JOIN goods g ON c.good_id = g.id WHERE c.email = #{email} ORDER BY c.create_time DESC")
    List<CartVO> selectCartVO(@Param("email") String email);
}
