package com.mapper;

import com.MyMapper;
import com.pojo.entity.Goods;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper extends MyMapper<Goods> {

    Goods getGoodsForOrderAmount(@Param("payAmount")String payAmount);
}
