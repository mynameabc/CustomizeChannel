package com.auxiliary.constant;

public class ProjectConstant {

    /**
     * 客户端收货专用小号们被存入redis, 此值就作为key
     */
    public static final String redisClientUserNameKey = "redisClientUserNameKey:";

    public static final String RedissonPayOrderKey = "payOrder:";


    public final static String privateKey = "privateKey";            //私钥
    public final static String superiorLimit = "superiorLimit";      //小号每日下单量
    public final static String heartBeatStatus = "heartBeatStatus";  //平台心跳开关 0:关, 1:开
    public final static String payOrderStatus = "payOrderStatus";    //平台下单开关 0:关, 1:开

}
