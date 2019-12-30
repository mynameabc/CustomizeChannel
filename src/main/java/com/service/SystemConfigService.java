package com.service;

import com.mapper.SystemConfigMapper;
import com.pojo.entity.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 下单开关是否开启
     * @return
     */
    public boolean isOpen() {

        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setKey("payOrderStatus");
        systemConfig = systemConfigMapper.selectOne(systemConfig);

        if (null != systemConfig) {

            String value = systemConfig.getValue();
            if (value.equals("1")) {
                return true;
            }
        }

        return false;
    }
}
