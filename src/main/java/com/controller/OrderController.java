package com.controller;

import com.pojo.dto.OrderDTO;
import com.service.OrderService;
import com.service.SystemConfigService;
import communal.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Api(tags="订单")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 设置支付地址
     * @param resultJSONString
     * @return
     */
    @RequestMapping(value = "setPayURL", method = RequestMethod.POST)
    public Result setPayURL(String resultJSONString) {
        return orderService.setPayURL(resultJSONString);
    }

    /**
     * 下单
     * @param orderDTO
     * @return
     * @throws IOException
     */
    @PostMapping(value = "pay")
    public Result pay(@Valid @RequestBody OrderDTO orderDTO) {
/*
        //下单开关
        boolean isvalue = systemConfigService.isOpen();
        if (isvalue == false) {
            return new Result(false, "系统下单开关被关闭, 请和管理员联系!");
        }
*/
        //ip判断

        log.info("接收到的订单参数:{}", orderDTO.toString());
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        orderDTO.setPlatformOrderNo(uuid);
        return orderService.pay(orderDTO);
    }

    /**
     * 通知成功回调
     * @param resultJSONString
     */
    @RequestMapping(value = "notify_res", method = RequestMethod.POST)
    public Result notify_res(String resultJSONString) {
        log.info("回调被启动!---值是:{}", resultJSONString);
        return orderService.notify(resultJSONString);
    }

    /**
     * 模似下游回调函数
     * @param
     */
    @RequestMapping(value = "xiayou_notify_res", method = RequestMethod.POST)
    public String xiayou_notify_res(@RequestBody String resultJSONString) {
        log.info("{}---下游回调函数!", resultJSONString);
        return "success";
    }

    @RequestMapping(value = "t", method = RequestMethod.POST)
    public String t(@RequestBody String resultJSONString) {
        log.info("测试:{}", resultJSONString);
        return resultJSONString;
    }
}
