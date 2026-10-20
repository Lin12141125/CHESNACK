package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.common.BusinessException;
import com.chesnake.dto.CommentAddDTO;
import com.chesnake.dto.CommentReplyDTO;
import com.chesnake.entity.Comment;
import com.chesnake.entity.Orders;
import com.chesnake.mapper.CommentMapper;
import com.chesnake.mapper.OrdersMapper;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final OrdersMapper ordersMapper;
    private final TransactionTemplate transactionTemplate;

    /** 商品评价列表（一级 + 二级回复树形结构），level: 1-Great 2-Normal 3-Bad */
    public List<Comment> listByGoods(Long goodsId, Integer level) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getGoodsId, goodsId)
                .orderByDesc(Comment::getSendTime);
        if (level != null && level >= 1 && level <= 3) {
            switch (level) {
                case 1 -> wrapper.and(w -> w.eq(Comment::getLevel, 5).or().isNull(Comment::getLevel));
                case 2 -> wrapper.and(w -> w.in(Comment::getLevel, 3, 4).or().isNull(Comment::getLevel));
                default -> wrapper.and(w -> w.in(Comment::getLevel, 1, 2).or().isNull(Comment::getLevel));
            }
        }
        return buildTree(commentMapper.selectList(wrapper));
    }

    private List<Comment> buildTree(List<Comment> records) {
        List<Comment> parents = new ArrayList<>();
        Map<Long, Comment> map = new HashMap<>();
        for (Comment c : records) {
            if (c.getPid() == null) {
                parents.add(c);
                map.put(c.getId(), c);
            }
        }
        for (Comment c : records) {
            if (c.getPid() == null) continue;
            Comment parent = map.get(c.getPid());
            if (parent != null) {
                if (parent.getReplies() == null) parent.setReplies(new ArrayList<>());
                parent.getReplies().add(c);
            }
        }
        return parents;
    }

    /** 星级分布统计 */
    public List<Map<String, Object>> countStats(Long goodsId) {
        List<Comment> list = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getGoodsId, goodsId).isNull(Comment::getPid));
        int great = 0, normal = 0, bad = 0;
        for (Comment c : list) {
            if (c.getLevel() == null || c.getLevel() >= 5) great++;
            else if (c.getLevel() >= 3) normal++;
            else bad++;
        }
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("label", "Great", "count", great, "level", 1));
        stats.add(Map.of("label", "Normal", "count", normal, "level", 2));
        stats.add(Map.of("label", "Bad", "count", bad, "level", 3));
        return stats;
    }

    /** 提交订单评价（事务 + 订单行锁：并发重复评价被串行化，后到者直接拒绝） */
    public void add(String username, CommentAddDTO dto) {
        transactionTemplate.executeWithoutResult(status -> doAdd(username, dto));
    }

    private void doAdd(String username, CommentAddDTO dto) {
        // 行锁锁定订单：同一订单的并发评价串行执行，第二个事务能看到已提交的评价
        Orders order = ordersMapper.selectByIdForUpdate(dto.orderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 越权防护：只能评价自己的订单
        if (!order.getEmail().equals(SecurityUtil.currentEmail())) {
            throw new BusinessException("只能评价自己的订单");
        }
        Long count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getOrderId, dto.orderId()));
        if (count != null && count > 0) {
            throw new BusinessException("该订单已评价");
        }
        Comment comment = new Comment();
        comment.setUsername(username);
        comment.setContent(dto.content());
        comment.setGoodsId(order.getGoodId());
        comment.setOrderId(dto.orderId());
        comment.setLevel(dto.level() == null ? 5 : dto.level());
        comment.setReported(0);
        commentMapper.insert(comment);
    }

    /** 回复评论 */
    public void reply(String username, CommentReplyDTO dto) {
        Comment parent = commentMapper.selectById(dto.commentId());
        if (parent == null) {
            throw new BusinessException("被回复的评论不存在");
        }
        Comment reply = new Comment();
        reply.setUsername(username);
        reply.setContent(dto.content());
        // 以父评论的商品为准，防止客户端传其他 goodsId 把回复挂到别的商品评价列表下
        reply.setGoodsId(parent.getGoodsId());
        reply.setOrderId(parent.getOrderId());
        reply.setPid(parent.getId());
        reply.setReported(0);
        commentMapper.insert(reply);
    }

    /** 举报评论 */
    public void report(Long id) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setReported(1);
        commentMapper.updateById(comment);
    }
}
