package com.pojo.customize;

import lombok.Data;

@Data
public class OrderInfo implements java.io.Serializable {

    /**
     * 平台订单号
     */
    private String platformOrderNo;

    /**
     * 支付用的URL
     */
    private String payUrl;

    /**
     * 客户端执行状态
     */
    private String clientOrderStatus;
}
