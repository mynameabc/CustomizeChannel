package com.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author
 **/
public class SignUtil {

    public static String sign(Map<String, String> model, String key) {
        model.remove("sign");
        String sign = DigestUtils.md5Hex(toUrlString(model, key)).toUpperCase();
        return sign;
    }

    public static boolean verifySign(Map<String, String> model, String key) {
        String sign = model.remove("sign");
        if (sign(model, key).equalsIgnoreCase(sign)) {
            return true;
        }
        return false;
    }

    public static String toUrlString(Map<String, String> model, String key) {
        Map<String, String> map = new TreeMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        StringBuilder append = stringBuilder.append("key=").append(key);
        System.out.println("====> SignUtil 原始签名Str：" + append);
        return append.toString();
    }

    public static String toUrlString(Map<String, String> model) {
        Map<String, String> map = new TreeMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static String toUrlObjectString(Map<String, Object> model, String key) {
        Map<String, Object> map = new TreeMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue()) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        stringBuilder.append("key=").append(key);
        return stringBuilder.toString();
    }


    public static String toUrlObjectString(Map<String, Object> model) {
        Map<String, Object> map = new TreeMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (null != entry.getValue()) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static String linkedToUrlString(Map<String, String> model) {
        Map<String, String> map = new LinkedHashMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }


    public static TreeMap signToMap(String str) {
        TreeMap<String, Object> map = transStringToMap(str, "&", "=");
        return map;
    }

    public static TreeMap<String, Object> transStringToMap(String mapString, String separator, String pairSeparator) {
        TreeMap<String, Object> map = new TreeMap<>();
        String[] fSplit = mapString.split(separator);
        for (int i = 0; i < fSplit.length; i++) {
            if (fSplit[i] == null || fSplit[i].length() == 0) {
                continue;
            }
            String[] sSplit = fSplit[i].split(pairSeparator);
            String value = fSplit[i].substring(fSplit[i].indexOf('=') + 1, fSplit[i].length());
            map.put(sSplit[0], value);
        }

        return map;
    }

    public static String addKeyToUrlString(Map<String, String> model) {
        Map<String, String> map = new LinkedHashMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static String linkedToUrlString(Map<String, String> model, String key) {
        Map<String, String> map = new LinkedHashMap<>(model);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()).append("&");
            }
        }
        stringBuilder.append("key=").append(key);
        return stringBuilder.toString();
    }


    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();
        map.put("merchantNo", "test");
        map.put("outTradeNo", "123162747");
        String sign = SignUtil.sign(map, "UCYAWXVPV7FV4HU7");   //加签
        map.put("sign", sign);
        System.out.println("sign=" + sign);

        //验签
        boolean verify = SignUtil.verifySign(map, "UCYAWXVPV7FV4HU7");
        System.out.println("verify=" + verify);
    }
}
