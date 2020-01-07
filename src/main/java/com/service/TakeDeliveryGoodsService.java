package com.service;

import com.alibaba.fastjson.JSONObject;
import com.mapper.PayOrderMapper;
import com.pojo.customize.Client;
import com.pojo.customize.TakeDeliveryGoods;
import com.pojo.entity.PayOrder;
import com.utils.WebSocketSendObject;
import com.websokcet.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class TakeDeliveryGoodsService {

    @Autowired
    private PayOrderMapper payOrderMapper;

    public void manualReceiving(String platformOrderNo) {


    }

    /**
     * 发送收货消息到客户端
     */
    public void doAction() {

        JSONObject params = WebSocketSendObject.sendObjectForJSONObject("5");
        ConcurrentHashMap<String, Client> websocketMap = WebSocket.getWebsocketMap();
        List<Client> list = new ArrayList<>(websocketMap.values());
        for (Client client : list) {

            List<TakeDeliveryGoods>payOrderList = payOrderMapper.getTakeDeliveryGoodsList(client.getPlaceOrderName());
            for (TakeDeliveryGoods takeDeliveryGoods : payOrderList) {

                {
                    //下单小号
                    params.put("user_name", takeDeliveryGoods.getName());
                    //下单小号密码
                    params.put("password", takeDeliveryGoods.getPassword());
                    //国美订单号
                    params.put("client_order_no", takeDeliveryGoods.getClientOrderNo());
                    //ip
                    params.put("proxy_ip", takeDeliveryGoods.getProxyIp());
                    //ip端口号
                    params.put("proxy_port", takeDeliveryGoods.getProxyPort());
                    //ip登陆账号
                    params.put("proxy_user", takeDeliveryGoods.getProxyUser());
                    //ip登陆密码
                    params.put("proxy_psw", takeDeliveryGoods.getProxyPsw());
                }

                new Thread() {
                    @Override
                    public void run() {
                        WebSocket.sendMessage(takeDeliveryGoods.getClientName(), params.toJSONString());
                        log.info("本次发送收货的平台订单号是:{}---前线订单号是:{}", takeDeliveryGoods.getPlatformOrderNo(), takeDeliveryGoods.getClientOrderNo());
                    }
                }.start();
            }
        }
    }
}
