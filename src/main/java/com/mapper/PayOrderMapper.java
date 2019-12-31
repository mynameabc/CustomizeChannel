package com.mapper;

import com.MyMapper;
import com.pojo.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayOrderMapper extends MyMapper<PayOrder> {

    PayOrder getOrderForPlatformOrderNo(@Param("platformOrderNo")String platformOrderNo);

    List<PayOrder> getOrderForStatus(@Param("status")String status);

    PayOrder getOrderForClientOrderNo(@Param("clientOrderNo")String clientOrderNo);

    void setNumberIni();
}