package com.system;

import com.service.GoodsService;
import com.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RunLoading implements ApplicationRunner {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public void run(ApplicationArguments args) {
        goodsService.refresh();
        log.info("加载商品信息到redis缓存!");
        systemConfigService.refresh();
        log.info("加载系统配置表到redis缓存!");
    }
}
