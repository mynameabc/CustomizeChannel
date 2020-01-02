package com.service;

import com.mapper.SystemConfigMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final static String privateKey = "sysconfig:privateKey";            //私钥
    private final static String superiorLimit = "sysconfig:superiorLimit";      //小号每日下单量
    private final static String payOrderStatus = "sysconfig:payOrderStatus";    //平台下单开关 0:关, 1:开

    /**
     * 开启下单开关
     */
    public void placeOrderOpen() {
        systemConfigMapper.placeOrderOpenOrColse("1");
        stringRedisTemplate.opsForValue().set(payOrderStatus, "1");
    }

    /**
     * 关闭下单开关
     */
    public void placeOrderClose() {
        systemConfigMapper.placeOrderOpenOrColse("0");
        stringRedisTemplate.opsForValue().set(payOrderStatus, "0");
    }

    /**
     * 下单开关是否开启 (true:开, false:关)
     * @return
     */
    public boolean isOpen() {
        String value = stringRedisTemplate.opsForValue().get(payOrderStatus);
        if (StringUtils.isBlank(value)) {
            value = systemConfigMapper.isOpen();
            stringRedisTemplate.opsForValue().set(payOrderStatus, value);
        }
        return (value.equals("1")) ? (true) : (false);
    }
}
