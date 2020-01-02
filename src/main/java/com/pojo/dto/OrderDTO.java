package com.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderDTO implements java.io.Serializable {

    @ApiModelProperty(value="平台订单号", name="platformOrderNo", required=true, example="2019072901956915958")
    private String platformOrderNo;

    @ApiModelProperty(value="支付方式", name="payType", required=true, example="支付方式:(0:支付宝, 1:支付宝WAP, 2:微信扫一扫, 3:微信H5, 5:QQ, 6:QQWAP, 7:京东, 8:京东WAP, 9:银联快捷, 10:银联快捷WAP, 11:银联网关, 12:银联扫码, 13:苏宁支付)")
    private String payType;

    @ApiModelProperty(value="订单金额", name="amount", required=true, example="30")
    private String amount;

    @ApiModelProperty(value="通道", name="channel", required=true, example="目前只有GuoMei")
    private String channel;

    @ApiModelProperty(value="异步通知地址", name="notifyUrl", required=true, example="http://www.baidu.com/notify_res")
    private String notifyUrl;

    @ApiModelProperty(value="加签字符串", name="sign", required=true)
    private String sign;
}
