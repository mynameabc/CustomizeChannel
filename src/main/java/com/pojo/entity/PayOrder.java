package com.pojo.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "pay_order")
public class PayOrder implements java.io.Serializable {

    private static final long serialVersionUID = -7884092935032142354L;

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "channel")
    private String channel;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "amount")
    private String amount;

    @Column(name = "platform_order_no")
    private String platformOrderNo;

    @Column(name = "client_order_status")
    private String clientOrderStatus;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "status")
    private String status;

    @Column(name = "receiving_goods_status")
    private String receivingGoodsStatus;

    @Column(name = "notify_send_notify_count")
    private Integer notifySendNotifyCount;

    @Column(name = "goods_url")
    private String goodsUrl;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "client_order_no")
    private String clientOrderNo;

    @Column(name = "pay_url")
    private String payUrl;

    @Column(name = "sign")
    private String sign;

    @Column(name = "return_result")
    private String returnResult;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "pay_time")
    private Date payTime;

    @Column(name = "receiving_goods_time")
    private Date receivingGoodsTime;
}
