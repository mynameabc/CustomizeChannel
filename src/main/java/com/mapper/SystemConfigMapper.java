package com.mapper;

import com.MyMapper;
import com.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SystemConfigMapper extends MyMapper<SystemConfig> {

    int propertiesOpenOrClose(@Param("key")String key, @Param("value")String value);

    String getSystemConfigValue(@Param("name") String name);
}
