package com.chesnake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chesnake.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 扣库存（乐观：库存充足才更新）
     */
    @Update("UPDATE goods SET stock = stock - #{num}, sale_quantity = sale_quantity + #{num} " +
            "WHERE id = #{goodId} AND stock >= #{num}")
    int deductStock(@Param("goodId") Long goodId, @Param("num") int num);

    /**
     * 恢复库存（订单插入撞唯一索引等异常场景：把本次已扣的库存加回，避免库存凭空流失）
     */
    @Update("UPDATE goods SET stock = stock + #{num}, sale_quantity = sale_quantity - #{num} " +
            "WHERE id = #{goodId}")
    int restoreStock(@Param("goodId") Long goodId, @Param("num") int num);

    @Select("SELECT DISTINCT brand FROM goods WHERE brand IS NOT NULL AND brand <> '' ORDER BY brand")
    List<String> selectBrands();

    @Select("SELECT DISTINCT type FROM goods ORDER BY type")
    List<String> selectTypes();

    @Select("SELECT DISTINCT origin FROM goods ORDER BY origin")
    List<String> selectOrigins();
}
