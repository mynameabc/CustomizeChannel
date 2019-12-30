package com.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 三方与前线交互的对象
 */
@Data
public class FrontlineInteractiveDTO implements java.io.Serializable {

    @ApiModelProperty(value="命令", name="command", required=true)
    private String command;

    private String channel;

    private String pay_type;

    private String amount;

    private String platform_order_no;

    private String goods_url;

    private String user_name;

    private String password;

    private String client_order_no;

    private String pay_url;

    private String client_order_status;

    @ApiModelProperty(value="回调函数", name="notify_url", required=true)
    private String notify_url;
}
