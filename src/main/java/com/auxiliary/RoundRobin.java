package com.auxiliary;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoundRobin {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String key = "roundRobin";

    public int get(List list) {

        String numberStr = "0";
        numberStr = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(numberStr)) {
            numberStr = "0";
            stringRedisTemplate.opsForValue().set(key, numberStr);
        }

        int number = Integer.valueOf(numberStr);
        if (number < list.size()) {

            number++;
            if (number >= list.size()) {
                number = 0;
            }

        } else {
            number = 0;
        }

        stringRedisTemplate.opsForValue().set(key, String.valueOf(number));

        return number;
    }
}
