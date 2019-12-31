package com.job;

import com.mapper.PayOrderMapper;
import com.mapper.SystemConfigMapper;
import com.service.OrderService;
import com.service.SystemConfigService;
import com.service.TakeDeliveryGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

@Slf4j
public class TestQuartz extends QuartzJobBean {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TakeDeliveryGoodsService takeDeliveryGoodsService;

    @Autowired
    private PayOrderMapper payOrderMapper;

    /**
     * 执行定时任务
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("quartz task : {}" , new Date());

        //关闭系统下单开关
        systemConfigService.open();

        {
            //睡5分钟
        }

        //设置client_user表的number为0
        payOrderMapper.setNumberIni();

        takeDeliveryGoodsService.doAction();    //调用收货
    }
}
