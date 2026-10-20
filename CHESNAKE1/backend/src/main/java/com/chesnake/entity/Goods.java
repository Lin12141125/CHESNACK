package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String name;
    private String origin;
    private String brand;
    private Integer netContent;
    private BigDecimal price;
    private Integer saleQuantity;
    private Integer stock;
    private String img1;
    private String img2;
    private Integer score;
}
