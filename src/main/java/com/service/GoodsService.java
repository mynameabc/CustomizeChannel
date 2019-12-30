package com.service;

import com.pojo.entity.Goods;
import com.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    public Goods getGoodsForAmount(String amount) {
        Goods goods = new Goods();
        goods.setPayAmount(amount);
        goods = goodsMapper.selectOne(goods);
        return goods;
    }
}
