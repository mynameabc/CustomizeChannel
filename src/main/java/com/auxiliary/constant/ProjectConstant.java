package com.auxiliary.constant;

/**
 * @author Administrator
 */
public class ProjectConstant {

    /**
     * 表示:否 失败
     */
    public static final String FAIL = "0";

    /**
     * 表示:是 同意 成功
     */
    public static final String SUCCESS = "1";




    public static final String redisClientUserNameKey = "redisClientUserNameKey:";

    /**
     * 这个Key用于存放 OrderInfo 对象
     */
    public static final String PAY_ORDER_KEY = "PayOrder:";

    public static final String GOODS_KEY_PAY_AMOUNT = "goods:payAmount:";

    //*************************************************************************************//

    /**
     * redis定义的Map对象名
     */
    public final static String SYSTEM_CONFIG_MAP = "SysConfig";

    /**
     * 私钥
     */
    public final static String PRIVATE_KEY = "privateKey";

    /**
     * 小号每日下单量
     */
    public final static String SUPERIOR_LIMIT = "superiorLimit";

    /**
     * 平台心跳开关 0:关, 1:开
     */
    public final static String HEARTBEAT_STATUS = "heartBeatStatus";

    /**
     * 平台下单开关 0:关, 1:开
     */
    public final static String PAY_ORDER_STATUS = "payOrderStatus";

    /**
     * 下单IP白名单
     */
    public final static String PAY_ORDER_IP_WHITE = "payOrderIPWhite";

    //*************************************************************************************//
}
