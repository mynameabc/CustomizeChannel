package com.service;

import com.auxiliary.constant.ProjectConstant;
import com.mapper.GoodsMapper;
import com.pojo.entity.Goods;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedissonClient redissonClient;

    public void refresh() {
        RBucket<Goods> rBucket;
        List<Goods>goodList = goodsMapper.selectAll();
        for (Goods goods : goodList) {
            rBucket = redissonClient.getBucket(ProjectConstant.GOODS_KEY_PAY_AMOUNT + goods.getPayAmount());
            rBucket.set(goods);
        }
    }
}
