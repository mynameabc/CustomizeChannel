import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.auxiliary.test.NormalRoundRobinWebSocketImpl;
import com.pojo.customize.Client;
import com.pojo.customize.OrderInfo;
import com.utils.HttpClientUtil;
import com.utils.SignUtil;
import communal.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class Test {

    public static void main(String args[]) {

        String key = "52A1B74DDAFC4274992E51DDCDFCCD9F";

        Map<String, String> parmasMap = new HashMap<>(6);
        {
            parmasMap.put("command", "4");
            parmasMap.put("channel", "GuoMei");
            parmasMap.put("payType", "3");
            parmasMap.put("platformOrderNo", "2819100401572263346");
            parmasMap.put("amount", "9000");
            parmasMap.put("result", "OK");
            String sign = SignUtil.sign(parmasMap, key);
            parmasMap.put("sign", sign);
            log.info(sign);
            boolean isvalue = SignUtil.verifySign(parmasMap, key);
            System.out.println(isvalue);
        }

        String _temp = "amount=" + "9000" + "channel=" + "GuoMei" + "clientOrderStatus="
                + "0" + "platformOrderNo=" + "2019100401572263346" + "key=" + key;
        String _sign = MD5Util.MD5(_temp).toUpperCase();
        System.out.println(_temp);
        System.out.println(_sign);
/*
        parmasMap = new HashMap<>(6);
        parmasMap.put("command", "4");
        parmasMap.put("amount", "9000");
        parmasMap.put("channel", "GuoMei");
        parmasMap.put("password", "HUjunnan520");
        parmasMap.put("pay_type", "3");
        parmasMap.put("platform_order_no", "702001030121890742");
        parmasMap.put("client_order_no", "12130427376");
        parmasMap.put("goods_url", "http://item.m.gome.com.cn/product-A0006631091-pop8013099491.html");
        parmasMap.put("user_name", "16568285148");
        parmasMap.put("client_order_status", "0");
        parmasMap.put("notify_url", "http://47.75.188.136:8300/pay/guoMeiPlus/payNotify");
        parmasMap.put("key", "52A1B74DDAFC4274992E51DDCDFCCD9F");

        String sign = SignUtil.sign(parmasMap, key);
        parmasMap.put("sign", sign);
//        log.info(sign);
        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        System.out.println(isvalue);*/
//        System.out.println(sign);

/*
        Test test = new Test();
        for (int i = 0; i < 14; i++) {

            test.test();
        }
*/
/*
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int i1 = random.nextInt(10);
            System.out.println(i1);
        }
*/
/*
        Client client = new Client();
        client.setWebSocket(null);
        client.setPlaceOrderLoginStatus(0);                         //未登陆
        client.setClientUserName("userName");                         //客户端连接标识
        client.setPlaceOrderName("clientUser");             //下单小号
        client.setPlaceOrderPassword("Password");     //下单密码

        Client client1 = new Client();
        client1.setWebSocket(null);
        client1.setPlaceOrderLoginStatus(0);                         //未登陆
        client1.setClientUserName("userName1");                         //客户端连接标识
        client1.setPlaceOrderName("clientUser1");             //下单小号
        client1.setPlaceOrderPassword("Password1");     //下单密码

        Client client2 = new Client();
        client2.setWebSocket(null);
        client2.setPlaceOrderLoginStatus(1);                         //未登陆
        client2.setClientUserName("userName2");                         //客户端连接标识
        client2.setPlaceOrderName("clientUser2");             //下单小号
        client2.setPlaceOrderPassword("Password2");     //下单密码

        ConcurrentHashMap<String, Client> websocketMap = new ConcurrentHashMap<>();
        websocketMap.put("1", client);
        websocketMap.put("2", client1);
        websocketMap.put("3", client2);

        List<Client> list = new ArrayList<>(websocketMap.values());
        List<Client> list2 = list.stream().filter(Client->(Client.getPlaceOrderLoginStatus() == 1)).collect(Collectors.toList());

        for (int index = 0; index < list2.size(); index++) {

            Client client3 = list2.get(index);
            System.out.println(client3.getPlaceOrderLoginStatus());
            System.out.println(client3.getClientUserName());
            System.out.println(client3.getPlaceOrderName());
            System.out.println(client3.getPlaceOrderPassword());
        }

 */
/*
        String url = "http://localhost:8890/channel/pay";

        SortedMap<String, String> params = new TreeMap<>();

        params.put("platform_order_no", null);         //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单, 5:收货
        params.put("pay_type", "3");
        params.put("order_amount", "60");
        params.put("channel", "GuoMei");
        params.put("notify_url", "http://www.baidu.com/");
        params.put("sign", "grgfgsfdg");

        System.out.println(JSONObject.toJSONString(params, SerializerFeature.WriteMapNullValue));
*/
/*
        String resultJSONString = HttpClientUtil.sendPostRaw(url, params, "UTF-8");
        System.out.println(resultJSONString);

/*
        SortedMap<String, String> params = new TreeMap<>();

        params.put("command", "4");         //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单
        params.put("channel", "");
        params.put("pay_type", "");
        params.put("amount", "");
        params.put("platform_order_no", "");
        params.put("goods_url", "");
        params.put("user_name", "");
        params.put("password", "");
        params.put("client_order_no", "");      //国美订单号
        params.put("pay_url", "");              //支付联连
        params.put("clientOrderStatus", "");    //客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限)

        System.out.println(JSON.toJSONString(params));
        String sign = SignUtil.sign(params, "UCYAWXVPV7FV4HU7");   //加签
        params.put("sign", sign);
        System.out.println("加密后:" + JSON.toJSONString(params));
        boolean isvalue = SignUtil.verifySign(params, "UCYAWXVPV7FV4HU7");
        System.out.println(isvalue);
*/
/*        String resultJSONString = HttpClientUtil.sendPostRaw(url, params, "UTF-8");
        System.out.println(resultJSONString);*/
/*
        String result = HttpClientUtil.sendPostRaw("http://47.112.167.170/:8890/channel/xiayou_notify_res", null, "UTF-8");
        System.out.println(result);*/
    }
}
