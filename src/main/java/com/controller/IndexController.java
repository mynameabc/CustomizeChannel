package com.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Api(tags="首页")
@Controller
public class IndexController {

    @GetMapping(name = "index")
    public String index() {
        return "index";
    }

    @GetMapping(value = "webSocketLinke")
    public String webSocketLinke() {
        return "webSocketLinke";
    }
}
