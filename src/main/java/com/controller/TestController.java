package com.controller;

import com.websokcet.WebSocket;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

    @GetMapping(value = "test")
    public String test() {
        return WebSocket.getWebsocketMap().toString();
    }

    @GetMapping(value = "close")
    public void close(HttpServletRequest request) {
        String userName = request.getParameter("userName");
        WebSocket.singleClose(userName);
    }

    @GetMapping(value = "test2")
    public void test2(HttpServletRequest request) {
        WebSocket.test(request.getParameter("userName"));
    }

}


