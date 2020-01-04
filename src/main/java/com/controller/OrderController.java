package com.controller;

import com.pojo.dto.OrderDTO;
import com.service.OrderService;
import com.service.SystemConfigService;
import com.utils.SignUtil;
import communal.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
     * @param request
     * @return
     */
    @RequestMapping(value = "setPayURL", method = RequestMethod.POST)
    public Result setPayURL(HttpServletRequest request) {
        String resultJSONString = request.getParameter("resultJSONString");
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

        //下单开关
        if (!systemConfigService.isOpen()) {
            return new Result(false, "系统下单开关被关闭, 请和管理员联系!");
        }

        String key = "52A1B74DDAFC4274992E51DDCDFCCD9F";
        Map<String, String> parmasMap = new HashMap<>(6);
        {
            parmasMap.put("platformOrderNo", orderDTO.getPlatformOrderNo());
            parmasMap.put("payType", orderDTO.getPayType());
            parmasMap.put("amount", orderDTO.getAmount());
            parmasMap.put("channel", orderDTO.getChannel());
            parmasMap.put("notifyUrl", orderDTO.getNotifyUrl());
            parmasMap.put("sign", orderDTO.getSign());
        }

        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        if (!isvalue) {
            log.error("{}:该订单验签没通过!---{}", orderDTO.getPlatformOrderNo(), orderDTO.toString());
            return new Result(false, "参数错误!");
        }

        log.info("接收到的订单参数:{}", orderDTO.toString());
//        orderDTO.setPlatformOrderNo(UUID.randomUUID().toString().replaceAll("-",""));

        //ip判断
        return orderService.pay(orderDTO);
    }

    /**
     * 通知成功回调
     * @param resultJSONString
     */
    @RequestMapping(value = "notify_res", method = RequestMethod.POST)
    public Result notify_res(HttpServletRequest request, String resultJSONString) {

        log.info("回调被启动!---值是:{}", resultJSONString);
        String resultJSONString1 = request.getParameter("resultJSONString");
        return orderService.notify(resultJSONString1);
    }

    /**
     * 查询
     * @param orderDTO
     */
    @RequestMapping(value = "queryOrder", method = RequestMethod.POST)
    public Result queryOrder(@RequestBody OrderDTO orderDTO) {
        log.info("查询被启动!---值是:{}", orderDTO);
        return orderService.queryOrder(orderDTO);
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
}
