package com.controller;

import com.mapper.ClientUserMapper;
import com.pojo.customize.Client;
import com.service.SystemConfigService;
import com.service.TakeDeliveryGoodsService;
import com.websokcet.WebSocket;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.auxiliary.RoundRobin;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

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
        takeDeliveryGoodsService.doAction();
    }

    @GetMapping(value = "test2")
    public int test2() {
        return WebSocket.getWebsocketMap().size();
    }

    @GetMapping(value = "test3")
    public String test3() {
        return WebSocket.getWebsocketMap().toString();
    }

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

    @GetMapping(value = "test6")
    public void test6() {

        systemConfigService.placeOrderClose();          //关闭系统下单开关
        log.info("关闭下单功能!");

        {
            //睡2分钟
            try {
                Thread.sleep(1000 * 120);
            } catch (Exception e) {

            }
        }

        clientUserMapper.setNumberIni();      //设置client_user表的number为0
        takeDeliveryGoodsService.doAction();  //发送收货信息

        {
            //睡2分钟
            try {
                Thread.sleep(1000 * 120);
            } catch (Exception e) {

            }
        }

        //确认收货完毕

        systemConfigService.placeOrderOpen();           //打开系统下单开关
        log.info("关闭下单功能!");
    }
}


