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

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

    @GetMapping(value = "test")
    public String test() {
        return WebSocket.getWebsocketMap().toString();
    }

    @GetMapping(value = "test2")
    public void test6(HttpServletRequest request) {
        WebSocket.test(request.getParameter("userName"));
    }

}


