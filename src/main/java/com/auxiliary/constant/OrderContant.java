package com.auxiliary.constant;

public class OrderContant {

    //订单状态 status
    //*************************************************//

    /**
     * 支付连接生成成功
     */
    public static final String SUCCESS = "0";

    /**
     * 支付成功(收到回调)
     */
    public static final String PAY_SUCCESS = "1";

    /**
     * 支付连接生成失败
     */
    public static final String FAIL = "2";

    //*************************************************//

    /**
     * 未收货
     */
    public static final String RECEIVING_GOODS_STATUS_NO = "0";

    /**
     * :确认收货
     */
    public static final String RECEIVING_GOODS_STATUS_YES = "1";

}
