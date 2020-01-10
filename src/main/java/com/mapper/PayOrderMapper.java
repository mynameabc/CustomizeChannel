package com.mapper;

import com.MyMapper;
import com.pojo.customize.TakeDeliveryGoods;
import com.pojo.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayOrderMapper extends MyMapper<PayOrder> {

    PayOrder getOrderForPlatformOrderNo(@Param("platformOrderNo")String platformOrderNo);

    PayOrder getOrderForClientOrderNo(@Param("clientOrderNo")String clientOrderNo);

    List<TakeDeliveryGoods> getTakeDeliveryGoodsList(@Param("user_name")String userName);
}
