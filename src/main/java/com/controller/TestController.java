package com.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags="测试")
@RestController
public class TestController {

    @GetMapping(value = "test")
    public String test() {
        return "服务器已启动!";
    }
}

