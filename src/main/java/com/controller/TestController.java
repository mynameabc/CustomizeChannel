package com.controller;

import com.service.TakeDeliveryGoodsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

    @Autowired
    private TakeDeliveryGoodsService takeDeliveryGoodsService;

    @GetMapping(value = "test")
    public String test() {
        return "服务器已启动!";
    }

    @GetMapping(value = "test1")
    public void test1() {
        takeDeliveryGoodsService.doAction();
    }
}

