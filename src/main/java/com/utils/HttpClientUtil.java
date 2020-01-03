package com.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    public static void main(String args[]) {

        String key = "52A1B74DDAFC4274992E51DDCDFCCD9F";

        Map<String, String> parmasMap = new HashMap<>(6);
        {
            parmasMap.put("payType", "3");
            parmasMap.put("platformOrderNo", "172001030121890742");
            parmasMap.put("amount", "9000");
            parmasMap.put("channel", "GuoMei");
            parmasMap.put("notifyUrl", "http://47.75.188.136:8300/pay/guoMeiPlus/payNotify");
            String sign = SignUtil.sign(parmasMap, key);
            parmasMap.put("sign", sign);
//            boolean isvalue = SignUtil.verifySign(parmasMap, key);
//            System.out.println(isvalue);
        }

        String result = HttpClientUtil.sendPostRaw("http://47.112.167.170:8890/channel/pay", parmasMap, "UTF-8");
        System.out.println(result);


        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        System.out.println(isvalue);

    }

    /**
     * POST发送Http请求 (Raw方式)
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static String sendPostRaw(String url, Map params, String encoding) {

        //创建client和post对象
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        //json形式
        post.addHeader("content-type", "application/json;charset=utf-8");
        post.addHeader("accept","application/json");
        //json字符串以实体的实行放到post中
        post.setEntity(new StringEntity(JSONObject.toJSONString(params), Charset.forName(encoding)));
        HttpResponse response = null;
        try {
            //获得response对象
            response = client.execute(post);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(HttpStatus.SC_OK!=response.getStatusLine().getStatusCode()){
            System.out.println("请求返回不正确");
        }

        String result="";
        try {
            //获得字符串形式的结果
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 模拟请求
     *
     * @param url		资源地址
     * @param map	参数列表
     * @param encoding	编码
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String sendRow(String url, Map<String,Object> map, String encoding) {
        String body = "";

        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(map!=null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }

        try {
            //设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
        } catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("请求地址："+url);
        System.out.println("请求参数："+nvps.toString());

        //设置header信息
        //指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Content-type", "application/json");
//        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = null;
        try {
            //设置参数到请求对象中
            response = client.execute(httpPost);
        } catch (IOException e){
            e.printStackTrace();
        }

        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型

            try {
                //设置参数到请求对象中
                body = EntityUtils.toString(entity, encoding);
            } catch (IOException e){
                e.printStackTrace();
            }

        }

        try {
            EntityUtils.consume(entity);
            //释放链接
            response.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return body;
    }

    /**
     * 模拟请求
     *
     * @param url		资源地址
     * @param map	参数列表
     * @param encoding	编码
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String send(String url, Map<String,Object> map, String encoding) throws ParseException, IOException {
        String body = "";

        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(map!=null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        //设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));

        System.out.println("请求地址："+url);
        System.out.println("请求参数："+nvps.toString());

        //设置header信息
        //指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();
        return body;
    }

    public static String sendString(String url, Map<String,String> map, String encoding) throws ParseException, IOException {
        String body = "";

        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(map!=null){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        //设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));

        System.out.println("请求地址："+url);
        System.out.println("请求参数："+nvps.toString());

        //设置header信息
        //指定报文头【Content-type】、【User-Agent】
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();
        return body;
    }
}
