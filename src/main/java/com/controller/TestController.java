package com.controller;

import com.mapper.ClientUserMapper;
import com.pojo.customize.Client;
import com.service.SystemConfigService;
import com.service.TakeDeliveryGoodsService;
import com.websokcet.WebSocket;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.auxiliary.RoundRobin;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RoundRobin roundRobin;

    @Autowired
    private TakeDeliveryGoodsService takeDeliveryGoodsService;

    @Autowired
    private ClientUserMapper clientUserMapper;

    @GetMapping(value = "test")
    public String test() {
        return "服务器已启动!";
    }

    @GetMapping(value = "test1")
    public void test1() {
        WebSocket.getWebsocketMap().toString();
    }

    @GetMapping(value = "test2")
    public int test2() {
        return WebSocket.getWebsocketMap().size();
    }

    @GetMapping(value = "test3")
    public String test3() {
        return WebSocket.getWebsocketMap().toString();
    }
/*
    @GetMapping(value = "test4")
    public String test4() {
        return WebSocket.getClientUser().getClientUserName();
    }

    @GetMapping(value = "test5")
    public String test5() {
        Map map = WebSocket.getWebSocketUsablePlaceOrder();
        List<Client> list = new ArrayList<>(map.values());
        return list.get(roundRobin.get(list)).toString();
    }
*/
    @GetMapping(value = "test6")
    public void test6() {

    }

    @GetMapping(value = "test7")
    public String test7() {

        RBucket<String> keyObj = redissonClient.getBucket("sysconfig:aaa");
        log.info(keyObj.get());
        String status = keyObj.get();
        if (StringUtils.isBlank(status)) {  //未在下单
            keyObj.set("1", 1, TimeUnit.MINUTES);
            return keyObj.get();
        }

        return null;
    }
}


