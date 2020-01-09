package com.system;

import com.service.GoodsService;
import com.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public void run(ApplicationArguments args) {
        //加载系统配置表到redis缓存
        systemConfigService.refresh();
        //加载商品信息到redis缓存
        goodsService.refresh();
    }
}
