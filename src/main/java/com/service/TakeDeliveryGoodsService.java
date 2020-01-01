package com.service;

import com.alibaba.fastjson.JSONObject;
import com.auxiliary.constant.ProjectConstant;
import com.auxiliary.test.INormalRoundRobin;
import com.auxiliary.test.NormalRoundRobinImpl;
import com.mapper.PayOrderMapper;
import com.pojo.customize.Client;
import com.pojo.entity.PayOrder;
import com.utils.WebSocketSendObject;
import com.websokcet.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TakeDeliveryGoodsService {

    @Autowired
    private PayOrderMapper payOrderMapper;

    /**
     * 发送收货消息到客户端
     */
    public void doAction() {

        JSONObject params = WebSocketSendObject.sendObjectForJSONObject("5");
        ConcurrentHashMap<String, Client> websocketMap = WebSocket.getWebsocketMap();
        List<Client> list = new ArrayList<>(websocketMap.values());
        for (Client client : list) {

            List<PayOrder>payOrderList = payOrderMapper.getOrderForUserName(client.getPlaceOrderName());
            for (PayOrder payOrder : payOrderList) {

                {
                    params.put("user_name", payOrder.getUserName());            //下单小号
                    params.put("password", payOrder.getPassword());             //下单小号密码
                    params.put("client_order_no", payOrder.getClientOrderNo()); //国美订单号
                }

                new Thread() {
                    public void run() {
                        WebSocket.sendMessage(client.getClientUserName(), params.toJSONString());
                        log.info("本次发送收货的平台订单号是:{}---前线订单号是:{}", payOrder.getPlatformOrderNo(), payOrder.getClientOrderNo());
                    }
                }.start();
            }
        }
    }
}
