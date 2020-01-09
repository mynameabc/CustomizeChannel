package com.controller;

import com.auxiliary.constant.ProjectConstant;
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

@Slf4j
@Api(tags = "订单")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 设置支付地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "setPayURL", method = RequestMethod.POST)
    public Result setPayUrl(HttpServletRequest request) {
        String resultJSONString = request.getParameter("resultJSONString");
        log.info("setPayURL:" + resultJSONString);
        return orderService.setPayUrl(resultJSONString);
    }

    /**
     * 下单
     *
     * @param orderDTO
     * @return
     * @throws IOException
     */
    @PostMapping(value = "pay")
    public Result pay(@Valid @RequestBody OrderDTO orderDTO) {

        //下单开关
        if (!systemConfigService.isBoolean(ProjectConstant.PAY_ORDER_STATUS)) {
            return new Result(false, "系统下单开关被关闭, 请和管理员联系!");
        }

        //ip判断
        systemConfigService.getStringValue(ProjectConstant.PAY_ORDER_IP_WHITE);


        Map<String, String> paramsMap = new HashMap<>(6);
        {
            paramsMap.put("platformOrderNo", orderDTO.getPlatformOrderNo());
            paramsMap.put("payType", orderDTO.getPayType());
            paramsMap.put("amount", orderDTO.getAmount());
            paramsMap.put("channel", orderDTO.getChannel());
            paramsMap.put("notifyUrl", orderDTO.getNotifyUrl());
            paramsMap.put("sign", orderDTO.getSign());
        }

        log.info("接收到的订单参数:{}", orderDTO.toString());

        String key = systemConfigService.getStringValue(ProjectConstant.PRIVATE_KEY);
        boolean isValue = SignUtil.verifySign(paramsMap, key);
        if (!isValue) {
            log.error("{}:该订单验签没通过!---{}", orderDTO.getPlatformOrderNo(), orderDTO.toString());
            return new Result(false, "该订单验签没通过!");
        }

        return orderService.pay(orderDTO);
    }

    /**
     * 通知成功回调
     *
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
     *
     * @param orderDTO
     */
    @RequestMapping(value = "queryOrder", method = RequestMethod.POST)
    public Result queryOrder(@RequestBody OrderDTO orderDTO) {
        log.info("查询被启动!---值是:{}", orderDTO);
        return orderService.queryOrder(orderDTO);
    }

    /**
     * 模似下游回调函数
     *
     * @param
     */
    @RequestMapping(value = "xiayou_notify_res", method = RequestMethod.POST)
    public String xiayou_notify_res(@RequestBody String resultJSONString) {
        return orderService.xiayou_notify_res(resultJSONString);
    }
}
