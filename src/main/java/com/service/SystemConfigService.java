package com.service;

import com.auxiliary.constant.ProjectConstant;
import org.redisson.RedissonMap;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mapper.SystemConfigMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        return (String)redissonClient.getMap("sysconfig").get(value);
    }
}
