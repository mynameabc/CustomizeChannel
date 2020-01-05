package com.system;

import com.mapper.SystemConfigMapper;
import com.pojo.entity.SystemConfig;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    private final static String payOrderStatus = "sysconfig:payOrderStatus";    //平台下单开关 0:关, 1:开

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //加载系统配置表到redis缓存
        {
            RMap rMap = redissonClient.getMap("sysconfig");
            SystemConfig systemConfig = null;
            List<SystemConfig> systemConfigList = systemConfigMapper.selectAll();
            for (int index = 0; index < systemConfigList.size(); index++) {
                systemConfig = systemConfigList.get(index);
                rMap.put(systemConfig.getName(), systemConfig.getValue());
            }
        }
    }
}
