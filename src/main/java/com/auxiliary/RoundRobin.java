package com.auxiliary;

import com.auxiliary.constant.ProjectConstant;
import com.pojo.customize.Client;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RoundRobin {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String key = "roundRobin";

    public void test() {

        RBucket<String> serRBucket =
                redissonClient.getBucket(ProjectConstant.redisClientUserNameKey + "aaa");
    }

    public Client getClient(List<Client> list) {

        Object lock = "1";
        synchronized (lock) {

            if (null == list) {return null;}
            if (list.size() <= 0) {return null;}

            return poll(list);
/*
            String status = "";
            RBucket<String> serRBucket = null;
            for (Client _client : list) {

                serRBucket = redissonClient.getBucket(ProjectConstant.redisClientUserNameKey + _client.getPlaceOrderName());
                status = serRBucket.get();
                if (status.equals("1")) {
                    continue;
                } else {
                    serRBucket.set("1", 2, TimeUnit.MINUTES);
                    return _client;
                }
            }
 */
        }
    }

    public Client poll(List<Client> list) {

        int index = 0;
        String status;
        Client client = null;
        RBucket<String> serRBucket = null;
        for (Client _client : list) {

            index = get(list);
            client = list.get(index);

            serRBucket = redissonClient.getBucket(ProjectConstant.redisClientUserNameKey + _client.getPlaceOrderName());
            status = serRBucket.get();
            if (StringUtils.isBlank(status)) {  //未在忙
                serRBucket.set("1", 1, TimeUnit.MINUTES);   //让他忙
                return _client;
            } else {
                continue;
            }
        }

        return client;
    }

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
