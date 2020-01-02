package com.mapper;

import com.MyMapper;
import com.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

public interface SystemConfigMapper extends MyMapper<SystemConfig> {

    int placeOrderOpenOrColse(@Param("value")String value);

    String isOpen();
}