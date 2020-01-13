package com.controller;

import com.auxiliary.constant.ProjectConstant;
import com.pojo.dto.OrderDTO;
import com.service.OrderService;
import com.service.SystemConfigService;
import com.utils.SignUtil;
import communal.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * 获取真实IP
     * @param request
     * @return
     */
    private static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 下单
     *
     * @param orderDTO
     * @return
     * @throws IOException
     */
    @PostMapping(value = "pay")
    public Result pay(@Valid @RequestBody OrderDTO orderDTO, HttpServletRequest request) {

        //下单开关
        if (!systemConfigService.isBoolean(ProjectConstant.PAY_ORDER_STATUS)) {
            log.warn("系统下单开关被关闭, 请和管理员联系!");
            return new Result("00001", "系统下单开关被关闭, 请和管理员联系!");
        }

        //ip判断
        String clientIp = getIp(request);
        String ip = systemConfigService.getStringValue(ProjectConstant.PAY_ORDER_IP_WHITE);
        if (!clientIp.equals(ip)) {
            log.warn("下单客户端IP不在白名单中!");
            return new Result("00002", "下单客户端IP不在白名单中!");
        }

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
        boolean isValue = SignUtil.verifySign(paramsMap, systemConfigService.getStringValue(ProjectConstant.PRIVATE_KEY));
        if (!isValue) {
            log.error("{}:该订单验签没通过!---{}", orderDTO.getPlatformOrderNo(), orderDTO.toString());
            return new Result("00003", "该订单验签没通过!");
        }

        return orderService.pay(orderDTO);
    }

    /**
     * 通知成功回调
     * @param resultJsonString
     */
    @RequestMapping(value = "notifyRes", method = RequestMethod.POST)
    public Result notifyRes(HttpServletRequest request, String resultJsonString) {
        log.info("回调---参数:{}", resultJsonString);
        String jsonString = request.getParameter("resultJSONString");
        return orderService.notify(request.getParameter("resultJSONString"));
    }

    /**
     * 查询
     * @param orderDTO
     */
    @RequestMapping(value = "queryOrder", method = RequestMethod.POST)
    public Result queryOrder(@RequestBody OrderDTO orderDTO) {
        log.info("queryOrder查询---参数:{}", orderDTO);
        return orderService.queryOrder(orderDTO);
    }

    /**
     * 设置支付地址
     * @param request
     * @return
     */
    @RequestMapping(value = "setPayUrl", method = RequestMethod.POST)
    public Result setPayUrl(HttpServletRequest request) {
        log.info("setPayUrl:" + request.getParameter("jsonString"));
        return orderService.setPayUrl(request.getParameter("jsonString"));
    }
}
