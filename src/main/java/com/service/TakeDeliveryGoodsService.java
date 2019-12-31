package com.service;

import com.alibaba.fastjson.JSONObject;
import com.auxiliary.constant.ProjectConstant;
import com.auxiliary.test.INormalRoundRobin;
import com.auxiliary.test.NormalRoundRobinImpl;
import com.mapper.PayOrderMapper;
import com.pojo.entity.PayOrder;
import com.utils.WebSocketSendObject;
import com.websokcet.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;

@Slf4j
@Service
public class TakeDeliveryGoodsService {

    @Autowired
    private INormalRoundRobin normalRoundRobinImpl;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PayOrderMapper payOrderMapper;

    /**
     * 发送收货消息到客户端
     */
    public void doAction() {

        String clientUserName = null;
        JSONObject params = WebSocketSendObject.sendObjectForJSONObject("6");
        List<PayOrder> payOrderList = payOrderMapper.getOrderForStatus("5");
        for (PayOrder payOrder : payOrderList) {

            {
                params.put("user_name", payOrder.getUserName());            //下单小号
                params.put("password", payOrder.getPassword());             //下单小号密码
                params.put("client_order_no", payOrder.getClientOrderNo()); //国美订单号
            }

            clientUserName = this.getClientUserName();
            log.info("本次轮询到的收货账号是:{}", clientUserName);
            WebSocket.sendMessage(clientUserName, params.toJSONString());
        }
    }

    private String getClientUserName() {
        return (String) normalRoundRobinImpl.round();
    }
}
