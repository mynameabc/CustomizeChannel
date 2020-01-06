package com.controller;

import com.websokcet.WebSocket;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@Log4j2
@Api(tags="测试")
@RestController
public class TestController {

    @GetMapping(value = "test")
    public String test() {
        return WebSocket.getWebsocketMap().toString();
    }

    @GetMapping(value = "test2")
    public void test2(HttpServletRequest request) {
        WebSocket.test(request.getParameter("userName"));
    }

    @GetMapping(value = "close")
    public void close(HttpServletRequest request) {
        String userName = request.getParameter("userName");
        WebSocket.singleClose(userName);
    }

    @GetMapping(value = "test3")
    public void test3(HttpServletRequest request) {

        log.info("随便打");
        log.warn("随便打");
        log.debug("随便打");
        log.error("随便打");
    }


}


