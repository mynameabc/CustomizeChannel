package com.mapper;

import com.MyMapper;
import com.pojo.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends MyMapper<PayOrder> {

    PayOrder getOrderForPlatformOrderNo(@Param("platformOrderNo")String platformOrderNo);
}