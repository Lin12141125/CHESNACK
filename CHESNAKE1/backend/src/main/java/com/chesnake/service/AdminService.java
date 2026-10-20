package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.BusinessException;
import com.chesnake.config.RabbitConfig;
import com.chesnake.dto.GoodsSaveDTO;
import com.chesnake.entity.AdminLog;
import com.chesnake.entity.Comment;
import com.chesnake.entity.Goods;
import com.chesnake.mapper.AdminLogMapper;
import com.chesnake.mapper.CommentMapper;
import com.chesnake.mapper.GoodsMapper;
import com.chesnake.mq.AuditMessage;
import com.chesnake.search.GoodsSearchService;
import com.chesnake.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员服务（优化5+7+8 升级）：
 * - 商品变更走「双删 + 延迟双删」保证缓存一致性
 * - 商品变更同步 Elasticsearch 索引（增量）与 Redis 预扣库存视图
 * - 操作审计异步落库
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final GoodsMapper goodsMapper;
    private final CommentMapper commentMapper;
    private final AdminLogMapper adminLogMapper;
    private final RabbitTemplate rabbitTemplate;
    private final GoodsService goodsService;
    private final SeckillStockService stockService;
    private final GoodsSearchService goodsSearchService;

    // ================= 仓库管理 =================

    public Goods saveGoods(GoodsSaveDTO dto) {
        Goods goods = new Goods();
        goods.setId(dto.id());
        goods.setName(dto.name());
        goods.setType(dto.type());
        goods.setBrand(dto.brand());
        goods.setOrigin(dto.origin());
        goods.setNetContent(dto.netContent());
        goods.setPrice(dto.price());
        goods.setStock(dto.stock() == null ? 100 : dto.stock());
        goods.setSaleQuantity(dto.saleQuantity() == null ? 0 : dto.saleQuantity());
        goods.setScore(0);
        goods.setImg1(dto.img1());
        goods.setImg2(dto.img2());

        boolean isUpdate = dto.id() != null;
        if (isUpdate) {
            // 双删第一步：写库前删除旧缓存
            goodsService.deleteBeforeUpdate(goods.getId());
            goodsMapper.updateById(goods);
            audit("UPDATE_GOODS", "修改商品: id=" + goods.getId() + " " + goods.getName());
        } else {
            goodsMapper.insert(goods);
            audit("ADD_GOODS", "新增商品: " + goods.getName());
        }

        // 双删第二步：写库后立即删除 + 延迟双删（500ms 兜底并发窗口）
        goodsService.deleteAfterUpdate(goods.getId());

        // 秒杀库存视图同步（DB 为权威，覆盖式写入）
        stockService.syncStock(goods.getId(), goods.getStock());

        // Elasticsearch 增量同步
        goodsSearchService.syncOne(goodsMapper.selectById(goods.getId()));
        return goods;
    }

    public void deleteGoods(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goodsMapper.deleteById(id);
        goodsService.deleteAfterUpdate(id);
        stockService.removeStock(id);
        goodsSearchService.deleteOne(id);
        audit("DELETE_GOODS", "删除商品: id=" + id + " " + goods.getName());
    }

    // ================= 举报管理 =================

    public List<Comment> listReported() {
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getReported, 1)
                .orderByDesc(Comment::getSendTime));
    }

    public void deleteReportedComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        commentMapper.deleteById(id);
        audit("DELETE_COMMENT", "删除被举报评论: id=" + id);
    }

    // ================= 审计日志 =================

    public Page<AdminLog> logs(long page, long size) {
        return adminLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<AdminLog>().orderByDesc(AdminLog::getCreateTime));
    }

    private void audit(String action, String detail) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.AUDIT_ROUTING_KEY,
                new AuditMessage(SecurityUtil.currentUsername(), action, detail));
    }
}
