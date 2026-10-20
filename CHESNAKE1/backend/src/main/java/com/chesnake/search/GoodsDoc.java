package com.chesnake.search;

import java.math.BigDecimal;

/**
 * 商品搜索索引文档（Elasticsearch）
 */
public class GoodsDoc {

    public static final String INDEX = "chesnake-goods";

    private Long id;
    private String name;
    private String brand;
    private String type;
    private String origin;
    private BigDecimal price;
    private Integer saleQuantity;
    private Integer stock;
    private Integer score;
    private String img1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getSaleQuantity() { return saleQuantity; }
    public void setSaleQuantity(Integer saleQuantity) { this.saleQuantity = saleQuantity; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getImg1() { return img1; }
    public void setImg1(String img1) { this.img1 = img1; }
}
