package com.controller;

import com.alibaba.fastjson.JSONObject;
import communal.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags="收货")
@RestController
public class TakeDeliveryGoodsController {

    @PostMapping(name = "takeDeliveryGoods")
    public Result doAction(String clientOrderNo) {

        return null;
    }


    public JSONObject doAction() {



        return null;
    }
}
