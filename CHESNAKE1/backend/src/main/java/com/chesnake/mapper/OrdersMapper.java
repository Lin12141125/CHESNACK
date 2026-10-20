package com.chesnake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.entity.OrderVO;
import com.chesnake.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 订单分页（join 商品信息，一次查询返回，避免前端 N+1）
     * 支持关键词搜索（订单号/商品名）、日期区间筛选、是否已评价筛选
     */
    @Select("<script>" +
            "SELECT o.id, o.order_no, o.email, o.good_id, o.amount, o.price, o.status, o.create_time, " +
            "g.name AS goods_name, g.img1 AS goods_img, " +
            "EXISTS (SELECT 1 FROM comment c WHERE c.order_id = o.id) AS evaluated " +
            "FROM orders o JOIN goods g ON o.good_id = g.id " +
            "WHERE o.email = #{email} " +
            "<if test=\"keyword != null and keyword != ''\">" +
            " AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') OR g.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test=\"startDate != null and startDate != ''\"> AND o.create_time &gt;= #{startDate} </if>" +
            "<if test=\"endDate != null and endDate != ''\"> AND o.create_time &lt; DATE_ADD(#{endDate}, INTERVAL 1 DAY) </if>" +
            "<if test=\"evaluated != null\">" +
            "<choose>" +
            "<when test=\"evaluated == 1\"> AND EXISTS (SELECT 1 FROM comment c WHERE c.order_id = o.id) </when>" +
            "<otherwise> AND NOT EXISTS (SELECT 1 FROM comment c WHERE c.order_id = o.id) </otherwise>" +
            "</choose>" +
            "</if>" +
            " ORDER BY o.create_time DESC" +
            "</script>")
    Page<OrderVO> selectOrderVO(Page<OrderVO> page, @Param("email") String email,
                               @Param("keyword") String keyword, @Param("startDate") String startDate,
                               @Param("endDate") String endDate, @Param("evaluated") Integer evaluated);

    /** 行锁查询（评价前锁定订单行，串行化同一订单的并发评价） */
    @Select("SELECT * FROM orders WHERE id = #{id} FOR UPDATE")
    Orders selectByIdForUpdate(@Param("id") Long id);
}
