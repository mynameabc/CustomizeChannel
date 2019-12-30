package com.pojo.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "pay_order")
public class PayOrder implements java.io.Serializable {

    private static final long serialVersionUID = -7884092935032142354L;

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "platform_order_no")
    private String platformOrderNo;

    @Column(name = "client_order_no")
    private String clientOrderNo;

    @Column(name = "order_amount")
    private String orderAmount;

    @Column(name = "pay_amount")
    private String payAmount;

    @Column(name = "pay_order_url")
    private String payOrderUrl;

    @Column(name = "client_order_status")
    private String clientOrderStatus;

    @Column(name = "status")
    private String status;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "notify_par")
    private String notifyPar;

    @Column(name = "notify_send_notify_count")
    private Integer notifySendNotifyCount;

    @Column(name = "notify_last_send_time")
    private Date notifyLastSendTime;

    @Column(name = "return_result")
    private String returnResult;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}