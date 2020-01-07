package com.mapper;

import com.MyMapper;
import com.pojo.customize.TakeDeliveryGoods;
import com.pojo.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayOrderMapper extends MyMapper<PayOrder> {

    PayOrder getOrderForPlatformOrderNo(@Param("platformOrderNo")String platformOrderNo);

    List<PayOrder> getOrderForStatus(@Param("status")String status);

    PayOrder getOrderForClientOrderNo(@Param("clientOrderNo")String clientOrderNo);

    List<TakeDeliveryGoods> getOrderForUserName(@Param("userName")String userName);

    List<TakeDeliveryGoods> getTakeDeliveryGoodsList(@Param("user_name")String userName);
}
