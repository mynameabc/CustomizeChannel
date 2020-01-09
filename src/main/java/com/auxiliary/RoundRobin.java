package com.auxiliary;

import com.auxiliary.constant.ProjectConstant;
import com.pojo.customize.Client;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RoundRobin {

    @Autowired
    private RedissonClient redissonClient;

    public Client getClient(List<Client> list) {

        Object lock = "1";
        synchronized (lock) {

            String status;
            Client client;
            RBucket<String> rBucket;
            for (Client value : list) {

                client = value;
                rBucket = redissonClient.getBucket(ProjectConstant.redisClientUserNameKey + client.getPlaceOrderName());
                status = rBucket.get();
                if (StringUtils.isBlank(status)) {
                    //改变状态, 保持45秒
                    rBucket.set("1", 45, TimeUnit.SECONDS);
                    return client;
                } else {
                    continue;
                }
            }

            return null;
        }
    }
/*
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
 */
}
