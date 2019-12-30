package com.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.SortedMap;
import java.util.TreeMap;

public class WebSocketSendObject {

    public static SortedMap sendObjectForSortedMap(String command) {

        SortedMap<String, String> params = new TreeMap<>();

        {
            params.put("command", command);         //命令
            params.put("channel", "");              //通道(目前只有GuoMei)
            params.put("pay_type", "");             //支付方式
            params.put("amount", "");               //订单金额
            params.put("platform_order_no", "");    //平台订单号
            params.put("goods_url", "");            //商品URL
            params.put("user_name", "");            //下单小号
            params.put("password", "");             //下单小号密码
            params.put("client_order_no", "");      //国美订单号
            params.put("pay_url", "");              //支付联连
            params.put("client_order_status", "");  //客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限)
            params.put("notify_url", "");           //回调地址
            params.put("sign", "");
        }

        return params;
    }

    public static JSONObject sendObjectForJSONObject(String command) {

        JSONObject params = new JSONObject();

        {
            params.put("command", command);         //命令
            params.put("channel", "");              //通道(目前只有GuoMei)
            params.put("pay_type", "");             //支付方式
            params.put("amount", "");               //订单金额
            params.put("platform_order_no", "");    //平台订单号
            params.put("goods_url", "");            //商品URL
            params.put("user_name", "");            //下单小号
            params.put("password", "");             //下单小号密码
            params.put("client_order_no", "");      //国美订单号
            params.put("pay_url", "");              //支付联连
            params.put("client_order_status", "");  //客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限)
            params.put("notify_url", "");           //回调地址
            params.put("sign", "");
        }

        return params;
    }
}
