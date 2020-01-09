package com.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.SortedMap;
import java.util.TreeMap;

public class WebSocketSendObject {

    public static SortedMap<String, String> sendObjectForSortedMap(String command) {

        SortedMap<String, String> params = new TreeMap<>();

        //命令 0:心跳, 1:登陆, 2:登陆失败, 3:登陆成功, 4.下单, 5.收货, 6.确认收货
        params.put("command", command);
        //通道(目前只有GuoMei)
        params.put("channel", "");
        //支付方式
        params.put("pay_type", "");
        //订单金额
        params.put("amount", "");
        //平台订单号
        params.put("platform_order_no", "");
        //商品URL
        params.put("goods_url", "");
        //下单小号
        params.put("user_name", "");
        //下单小号密码
        params.put("password", "");
        //国美订单号
        params.put("client_order_no", "");
        //支付联连
        params.put("pay_url", "");
        //客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限)
        params.put("client_order_status", "");
        //回调地址
        params.put("notify_url", "");
        //加签字符串
        params.put("sign", "");
        //webSocket链接
        params.put("client_socket_id", "");

        return params;
    }

    public static JSONObject sendObjectForJSONObject(String command) {

        JSONObject params = new JSONObject(14);

        //命令 0:心跳, 1:登陆, 2:登陆失败, 3:登陆成功, 4.下单, 5.收货, 6.确认收货
        params.put("command", command);
        //通道(目前只有GuoMei)
        params.put("channel", "");
        //支付方式
        params.put("pay_type", "");
        //订单金额
        params.put("amount", "");
        //平台订单号
        params.put("platform_order_no", "");
        //商品URL
        params.put("goods_url", "");
        //下单小号
        params.put("user_name", "");
        //下单小号密码
        params.put("password", "");
        //国美订单号
        params.put("client_order_no", "");
        //支付联连
        params.put("pay_url", "");
        //客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限)
        params.put("client_order_status", "");
        //回调地址
        params.put("notify_url", "");
        //加签字符串
        params.put("sign", "");
        //webSocket链接
        params.put("client_socket_id", "");

        return params;
    }
}
