package com.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.SortedMap;
import java.util.TreeMap;

public class Test {

    public static String getDistanceTime(long time1, long time2) {

        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;

        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (day != 0) {
            return day + "天"+hour + "小时"+min + "分钟" + sec + "秒";
        }
        if (hour != 0) {
            return hour + "小时"+ min + "分钟" + sec + "秒";
        }
        if (min != 0) {
            return min + "分钟" + sec + "秒";
        }
        if (sec != 0) {
            return sec + "秒" ;
        }
        return "0秒";
    }



    public static void main(String args[]) {

        JSONObject object1 = new JSONObject();
        object1.put("channel", "GuoMei");
        object1.put("pay_type", "3");

        JSONObject order = new JSONObject();
        order.put("user_name", "16568285139");
        order.put("amount", "80");
        order.put("order_no", "A0006631151-pop8013104275");
        order.put("url", "http://item.gome.com.cn/A0006630274-pop8013103891.html");

        object1.put("order", order);

        System.out.println(object1.toJSONString());

        SortedMap<String, Object> params = new TreeMap<>();
        params.put("channel", "GuoMei");
        params.put("pay_type", "3");

        SortedMap<String, Object> _order = new TreeMap<>();
        _order.put("amount", "80");
        _order.put("user_name", "16568285139");
        _order.put("order_no", "A0006631151-pop8013104275");
        _order.put("url", "http://item.gome.com.cn/A0006630274-pop8013103891.html");

        params.put("order", _order);

        System.out.println(JSON.toJSONString(params));

        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        long diff;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;

        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(1000*3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long nowTime = System.currentTimeMillis();
        diff = nowTime - startTime;
        sec = diff % nd % nh % nm / ns;// 计算差多少秒

        System.out.println(sec);

    }
}
