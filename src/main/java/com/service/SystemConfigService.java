package com.service;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mapper.SystemConfigMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    public boolean isTrue(String key) {
        return (get(key).equals("1")) ? (true) : (false);
    }

    public String getSystemConfigValue(String key) {
        return get(key);
    }

    public void close(String key) {
        systemConfigMapper.propertiesOpenOrClose(key, "0");
        redissonClient.getMap("sysconfig").put(key, "0");
    }

    public void open(String key) {
        systemConfigMapper.propertiesOpenOrClose(key, "1");
        redissonClient.getMap("sysconfig").put(key, "1");
    }

    private String get(String key) {
        String value = (String)redissonClient.getMap("sysconfig").get(key);
        if (StringUtils.isBlank(value)) {
            value = systemConfigMapper.getSystemConfigValue(key);
            redissonClient.getMap("sysconfig").put(key, value);
        }
        return value;
    }
}
