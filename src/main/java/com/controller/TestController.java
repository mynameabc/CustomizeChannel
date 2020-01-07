package com.controller;

import com.service.TakeDeliveryGoodsService;
import com.websokcet.WebSocket;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@Log4j2
@Api(tags="测试")
@RestController
public class TestController {

    @Autowired
    private TakeDeliveryGoodsService takeDeliveryGoodsService;

    @GetMapping(value = "test")
    public String test() {
        return WebSocket.getWebsocketMap().toString();
    }

    @GetMapping(value = "test2")
    public void test2(HttpServletRequest request) {
        WebSocket.testSingleLogin(request.getParameter("userName"));
    }

    @GetMapping(value = "doAction")
    public void test3(HttpServletRequest request) {
        takeDeliveryGoodsService.doAction();
    }

    @GetMapping(value = "close")
    public void close(HttpServletRequest request) {
        String userName = request.getParameter("userName");
        WebSocket.singleClose(userName);
    }
}


