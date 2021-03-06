package com.mapper;

import com.MyMapper;
import com.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

public interface SystemConfigMapper extends MyMapper<SystemConfig> {

    int propertiesOpenOrClose(@Param("name")String name, @Param("value")String value);

    /**
     * 根据name返回value
     * @param name
     * @return
     */
    String getSystemConfigValue(@Param("name") String name);
}
