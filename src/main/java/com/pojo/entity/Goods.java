package com.pojo.entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;

@Data
@Table(name = "goods")
public class Goods implements java.io.Serializable {

    private static final long serialVersionUID = -7884092935032142352L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "goods_id")
    private String goodsId;

    @Column(name = "url")
    private String url;

    @Column(name = "pay_amount")
    private String payAmount;

    @Column(name = "create_time")
    private Date createTime;
}
