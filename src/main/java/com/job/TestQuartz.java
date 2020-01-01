package com.job;

import com.mapper.ClientUserMapper;
import com.mapper.PayOrderMapper;
import com.mapper.SystemConfigMapper;
import com.pojo.entity.ClientUser;
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

    @Autowired
    private ClientUserMapper clientUserMapper;

    /**
     * 执行定时任务
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        log.info("quartz task : {}" , new Date());
/*
        systemConfigService.close();          //关闭系统下单开关

        {
            //睡5分钟
        }

        clientUserMapper.setNumberIni();        //设置client_user表的number为0

        takeDeliveryGoodsService.doAction();  //发送收货信息

        {
            //睡5分钟
        }

        //确认收货完毕

        systemConfigService.open();           //打开系统下单开关
 */
    }
}
