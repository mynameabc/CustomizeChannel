package com.job;

import com.alibaba.fastjson.JSONObject;
import com.auxiliary.constant.ProjectConstant;
import com.mapper.ClientUserMapper;
import com.service.SystemConfigService;
import com.service.TakeDeliveryGoodsService;
import com.websokcet.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class Scheduler {

    private JSONObject jsonObject = new JSONObject();

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TakeDeliveryGoodsService takeDeliveryGoodsService;

    @Autowired
    private ClientUserMapper clientUserMapper;

    public Scheduler() {
        jsonObject.put("command", "0");
    }

    /**
     * 每隔20秒执行一次
     */
    @Scheduled(fixedRate = 20000)
    public void sendPing() {
        if (systemConfigService.isTrue(ProjectConstant.heartBeatStatus)) {
            WebSocket.sendPing(jsonObject.toString());
        }
    }

    /**
     * 每天24点执行 提醒收货job
     */
    @Scheduled(cron = "0 0 00 ? * *")
    public void OpenSystemTasks() {

        //打开系统下单开关
        systemConfigService.open(ProjectConstant.payOrderStatus);
        log.info("开启下单功能!");
    }

    /**
     * 每天23点45分执行 提醒收货job
     */
    @Scheduled(cron = "0 45 23 ? * *")
    public void TakeDeliveryTasks() {

        //关闭系统下单开关
        systemConfigService.close(ProjectConstant.payOrderStatus);
        log.info("关闭下单功能!");

        {
            //睡2分钟
            try {
                Thread.sleep(1000 * 120);
            } catch (Exception e) {

            }
        }

        clientUserMapper.setNumberIni();      //设置client_user表的number为0
//        takeDeliveryGoodsService.doAction();  //发送收货信息
    }
}
