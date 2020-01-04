package com.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auxiliary.RoundRobin;
import com.auxiliary.constant.ProjectConstant;
import com.mapper.ClientUserMapper;
import com.mapper.GoodsMapper;
import com.mapper.PayOrderMapper;
import com.pojo.customize.Client;
import com.pojo.customize.OrderInfo;
import com.pojo.dto.OrderDTO;
import com.pojo.entity.ClientUser;
import com.pojo.entity.Goods;
import com.pojo.entity.PayOrder;
import com.utils.HttpClientUtil;
import com.utils.SignUtil;
import com.utils.SnowflakeIdUtils;
import com.utils.WebSocketSendObject;
import com.websokcet.WebSocket;
import communal.Result;
import communal.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderService {

/*    static Logger logger = LoggerFactory.getLogger(OrderService.class);*/

    @Autowired
    private ClientUserMapper clientUserMapper;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RoundRobin roundRobin;

    private String key = "52A1B74DDAFC4274992E51DDCDFCCD9F";

    /**
     * 下单(自己四方调)
     * @param orderDTO
     * @return
     */
    public Result pay(OrderDTO orderDTO) {

        //金额是否正确
        log.info("订单数据:{}", orderDTO);
        Goods goods = goodsMapper.getGoodsForOrderAmount(orderDTO.getAmount());
        if (null == goods) {
            log.info("{}:不支持该金额", orderDTO.getPlatformOrderNo());
            return new Result(false, "不支持该金额");
        }

        //订单号是否存在
        PayOrder payOrder = this.getOrderForPlatformOrderNo(orderDTO.getPlatformOrderNo());
        if (null != payOrder) {
            log.info("{}:该平台订单已存在, 请不要重复下单.", orderDTO.getPlatformOrderNo());
            return new Result(false, "该平台订单已存在, 请不要重复下单.");
        }
/*
        //判断是否有可下单的WebSocket链接
        Map clientMap = WebSocket.getWebSocketUsablePlaceOrder();
        if (clientMap.size() <= 0) {
            log.info("{}:没有可用联接, 请和管理员联系", orderDTO.getPlatformOrderNo());
            return new Result(false, "没有可用联接, 请和管理员联系!");
        }
*/
        //轮询选出账号
        List<Client> list = WebSocket.getWebSocketUsablePlaceOrderList();
        Client client = roundRobin.getClient(list);
        if (null == client) {
            log.error("{}:没有可用下单小号!", orderDTO.getPlatformOrderNo());
            return new Result(false, "没有可用账号!");
        }

        log.info("WebSocket下单ID是:--------------------------------------------{}", client.getClientUserName());

        SortedMap<String, String> params = WebSocketSendObject.sendObjectForSortedMap("4");

        {
            params.put("channel", orderDTO.getChannel());
            params.put("pay_type", orderDTO.getPayType());
            params.put("amount", orderDTO.getAmount());
            params.put("platform_order_no", orderDTO.getPlatformOrderNo());
            params.put("goods_url", goods.getUrl());
            params.put("user_name", client.getPlaceOrderName());
            params.put("password", client.getPlaceOrderPassword());
            params.put("notify_url", orderDTO.getNotifyUrl());
        }

        String redisPlatformOrderNoKey = orderDTO.getPlatformOrderNo(); //以平台订单号为key
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setPlatformOrderNo(orderDTO.getPlatformOrderNo());
        orderInfo.setPayUrl("N");

        //发送WebSocket请求
        WebSocket.sendMessage(client.getClientUserName(), JSON.toJSONString(params));
        log.info("发送WebSocket请求给:{}", client.getClientUserName());

        //订单标识
        {
            RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + redisPlatformOrderNoKey);
            serRBucket.set(orderInfo, 1, TimeUnit.MINUTES);
        }

        log.info("开始监听:{}", orderDTO.getPlatformOrderNo());

        long sleep = 0L;
        for (int index = 1; index <= 6; index++) {

            sleep = 1000 * index;

            log.info("{}---第:{}循环:", orderDTO.getPlatformOrderNo(), index);

            try {
                Thread.sleep(sleep);
            } catch (Exception e) {}

            {
                RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + redisPlatformOrderNoKey);
                orderInfo = serRBucket.get();
            }

            if (!StringUtils.isBlank(orderInfo.getClientOrderStatus())) {
                break;
            }
        }

        if (null != orderInfo) {
            if (!orderInfo.getPayUrl().equals("N")) {
                RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + redisPlatformOrderNoKey);
                serRBucket.delete();
                return new Result(true, "成功!", orderInfo.getPayUrl());
            } else {
                RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + redisPlatformOrderNoKey);
                serRBucket.delete();
                return new Result(false, "请求超时!");
            }
        } else {
            RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + redisPlatformOrderNoKey);
            serRBucket.delete();
            return new Result(false, "下单异常, 请和管理员联系!");
        }
    }

    /**
     * 设置支付URL(前线调)
     * @param resultJSONString
     * @return
     */
    @Transactional
    public Result setPayURL(String resultJSONString) {

        log.info("setPayURL:" + resultJSONString);

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);
        String sign = jsonObject.getString("sign");                                 //加签后字符串
        String goods_url = jsonObject.getString("goods_url");                       //商品URL
        String channel = jsonObject.getString("channel");                           //充值渠道
        String user_name = jsonObject.getString("user_name");                       //下单小号
        String password = jsonObject.getString("password");                         //下单小号密码
        String pay_type = jsonObject.getString("pay_type");                         //支付方式
        String amount = jsonObject.getString("amount");                             //订单金额
        String clientOrderStatus = jsonObject.getString("client_order_status");     //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String pay_url = jsonObject.getString("pay_url");                           //支付地址
        String client_order_no = jsonObject.getString("client_order_no");           //国美订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");         //平台订单号
        String notify_url = jsonObject.getString("notify_url");                     //回调地址
        String client_socket_id = jsonObject.getString("client_socket_id");         //socket链接ID

        String _temp = "amount=" + amount + "channel=" + channel + "clientOrderStatus="
                + clientOrderStatus + "platformOrderNo=" + platformOrderNo + "key=" + key;
        String _sign = MD5Util.MD5(_temp).toUpperCase();
        if (!_sign.equals(sign)) {
            log.error("setPayURL方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "setPayURL参数错误!");
        }

        /*
        Map<String, String> parmasMap = (Map) jsonObject;
        parmasMap.remove("pay_url");
        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        if (!isvalue) {
            log.error("setPayURL方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "setPayURL参数错误!");
        }*/

        try {

            //取出缓存对象判断是否存在
            {
                RBucket<OrderInfo> serRBucket = redissonClient.getBucket(ProjectConstant.RedissonPayOrderKey + platformOrderNo);
                OrderInfo orderInfo = serRBucket.get();

                if (null == orderInfo) {
                    log.error("订单号:{}---该平台订单号不存在!---redis", platformOrderNo);
                    return new Result(false, "该平台订单号不存在");
                }

                if (StringUtils.isBlank(clientOrderStatus)) {
                    orderInfo.setClientOrderStatus("9");        //客户端状态返回空时给于一个状态
                } else {
                    orderInfo.setClientOrderStatus(clientOrderStatus);

                    ClientUser clientUser = clientUserMapper.getClientUserForName(user_name);
                    if (null == clientUser) {
                        log.error("订单号:{}---该下单小号不存在!", platformOrderNo);
                        return new Result(false, "该下单小号不存在!");
                    }

                    if (clientOrderStatus.equals("0")) {

                        int number = clientUser.getNumber();
                        clientUser.setNumber(++number);
                        clientUserMapper.updateByPrimaryKeySelective(clientUser);

                        //判断小号是否下满
                        {
                            int orderNumberCount = 20;
                            if (number >= orderNumberCount) {

                                //WebSocket里当前client对象的PlaceOrderStatus属性设成0(不可下单)
                                Client client = WebSocket.getClient(clientUser.getClientName());
                                client.setPlaceOrderStatus(0);
                                WebSocket.setClient(client);

                                //job跑client_user表的number设成0
                                //WebSocket里client对象的PlaceOrderStatus属性全设成1
                            }
                        }

                        orderInfo.setPayUrl(pay_url);
                    }
                }

                serRBucket.set(orderInfo, 30, TimeUnit.SECONDS);
            }

            //查看数据库是否存在该订单
            PayOrder payOrder = payOrderMapper.getOrderForPlatformOrderNo(platformOrderNo);
            if (null != payOrder) {
                log.error("订单号:{}---该平台订单号已在存!---DB", platformOrderNo);
                return new Result(false, "该平台订单号已在存!");
            }

            {
                String status = "";
                Date nowDate = new Date();
                SnowflakeIdUtils idWorker = new SnowflakeIdUtils(3, 1);

                log.info("订单号:{}的clientOrderStatus状态是:{}", platformOrderNo, clientOrderStatus);

                switch (clientOrderStatus)
                {
                    case "0":
                        status = "1";   //支付链接生成成功
                        break;
                    case "1":
                        status = "3";   //支付连接生成失败
                        break;
                    case "2":
                        status = "3";   //支付连接生成失败
                        break;
                    case "9":
                        status = "6";   //前线返回未知状态
                    default:
                        break;
                }

                payOrder = new PayOrder();
                {
                    payOrder.setOrderId(idWorker.nextId());
                    payOrder.setSign(sign);
                    payOrder.setGoodsUrl(goods_url);
                    payOrder.setChannel(channel);
                    payOrder.setPayType(pay_type);
                    payOrder.setAmount(amount);
                    payOrder.setPlatformOrderNo(platformOrderNo);
                    payOrder.setUserName(user_name);
                    payOrder.setPassword(password);
                    payOrder.setClientOrderNo(client_order_no);
                    payOrder.setPayUrl(pay_url);
                    payOrder.setClientOrderStatus(clientOrderStatus);
                    payOrder.setStatus(status);
                    payOrder.setNotifyUrl(notify_url);
                    payOrder.setNotifySendNotifyCount(0);
//                    payOrder.setNotifyLastSendTime();
//                    payOrder.setReturnResult("");
                    payOrder.setCreateTime(nowDate);
                    payOrderMapper.insert(payOrder);
                }

                log.info("-------------------设置支付URL方法成功-------------------:" + clientOrderStatus);
            }

        } catch (Exception e) {
            String logInfo = platformOrderNo + ":订单号:{}---记录添加异常!" + "\n";
            logInfo += "[异常信息]  - " + e.toString() + "\n";
            log.error(logInfo, platformOrderNo);
        }

        return new Result(true, platformOrderNo + ":" + "设置成功!");
    }

    public PayOrder getOrderForPlatformOrderNo(String platformOrderNo) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPlatformOrderNo(platformOrderNo);
        return payOrderMapper.selectOne(payOrder);
    }

    public Result queryOrder(OrderDTO orderDTO) {

        PayOrder payOrder = payOrderMapper.getOrderForPlatformOrderNo(orderDTO.getPlatformOrderNo());
        if (null == payOrder) {
            return new Result(false, "没找到这条订单!");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", payOrder.getStatus());
        jsonObject.put("platform_order_no", payOrder.getPlatformOrderNo());
        jsonObject.put("channel", payOrder.getChannel());
        jsonObject.put("pay_type", payOrder.getPayType());

        return new Result(true, "查询成功!", jsonObject.toJSONString());
    }

    /**
     * 通知成功回调
     * @param resultJSONString
     * @return
     */
    public Result notify(String resultJSONString) {

        log.info("notify方法中---{}", resultJSONString);

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);

        String notifyUrl = jsonObject.getString("notify_url");
        String sign = jsonObject.getString("sign");                                 //加签后字符串
        String goods_url = jsonObject.getString("goods_url");                       //商品URL
        String channel = jsonObject.getString("channel");                           //充值渠道
        String user_name = jsonObject.getString("user_name");                       //下单小号
        String password = jsonObject.getString("password");                         //下单小号密码
        String pay_type = jsonObject.getString("pay_type");                         //支付方式
        String amount = jsonObject.getString("amount");                             //订单金额
        String clientOrderStatus = jsonObject.getString("client_order_status");     //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String pay_url = jsonObject.getString("pay_url");                           //支付地址
        String client_order_no = jsonObject.getString("client_order_no");           //国美订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");         //平台订单号
/*
        Map<String, String> parmasMap = (Map) jsonObject;
        parmasMap.remove("pay_url");
        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        if (!isvalue) {
            log.error("notify方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "notify参数错误!");
        }
*/
        String _temp = "amount=" + amount + "channel=" + channel + "clientOrderStatus="
                + clientOrderStatus + "platformOrderNo=" + platformOrderNo + "key=" + key;
        String _sign = MD5Util.MD5(_temp).toUpperCase();
        if (!_sign.equals(sign)) {
            log.error("notify方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "notify参数错误!");
        }

/*
        SortedMap<String, String> params = new TreeMap<>();
        params.put("command", jsonObject.getString("command"));         //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单
        params.put("channel", jsonObject.getString("channel"));
        params.put("payType", jsonObject.getString("pay_type"));
        params.put("platformOrderNo", jsonObject.getString("platform_order_no"));
        params.put("amount", jsonObject.getString("amount"));
        params.put("result", "OK");
        String sign = SignUtil.sign(params, key);
        params.put("sign", sign);

        log.info("{}:发送给下游的参数:{}", params.get("platformOrderNo"), params);
        String notifyUrl1 = "http://localhost:8890/channel/xiayou_notify_res";

        //更新成功
        new Thread() {
            public void run(){
                String result = HttpClientUtil.sendPostRaw(notifyUrl1, params, "UTF-8");
                log.info("-------------------{}:订单号发送给下游回调的反回值:{}-------------------", platformOrderNo, result);
            }
        }.start();
        */

        //判断记录是否存在并且pay_order表状态是否是5
        PayOrder payOrder = payOrderMapper.getOrderForPlatformOrderNo(platformOrderNo);
        if (null == payOrder) {
            log.info("{}:没查到该订单号---回调方法!", platformOrderNo);
            return new Result(false, platformOrderNo + "没查到该订单号!");
        }

        //订单状态(1:支付连接生成成功, 2:成功(支付链接有返回给调用方), 3:支付连接生成失败, 4:请求超时, 5:支付成功, 6:前线返回未知状态)
        if (payOrder.getStatus().equals("5")) {
            log.info("{}:该订单号状态已设为支付成功---回调方法!", platformOrderNo);
            return new Result(false, platformOrderNo + ":已通知过, 不要重复发送!");
        }

        //订单状态是支付连接生成成功时
        if (payOrder.getStatus().equals("1")) {

            int count = 0;
            try {
                payOrder.setStatus("5");
                payOrder.setUpdateTime(new Date());
                count = payOrderMapper.updateByPrimaryKey(payOrder);
            } catch (Exception e) {
                String logInfo = platformOrderNo + ":订单号:{}---更新异常!" + "\n";
                logInfo += "[异常信息]  - " + e.toString() + "\n";
                log.error(logInfo, platformOrderNo);
            }

            SortedMap<String, String> params = new TreeMap<>();
            params.put("command", jsonObject.getString("command"));         //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单
            params.put("channel", jsonObject.getString("channel"));
            params.put("payType", jsonObject.getString("pay_type"));
            params.put("platformOrderNo", jsonObject.getString("platform_order_no"));
            params.put("amount", jsonObject.getString("amount"));
            params.put("result", "OK");
            String xsign = SignUtil.sign(params, key);
            params.put("sign", xsign);

            log.info("{}:发送给下游的参数:{}", params.get("platformOrderNo"), params);
//            String notifyUrl1 = "http://localhost:8890/channel/xiayou_notify_res";

            //更新成功
            if (count >= 1) {
                new Thread() {
                    public void run(){
                        String result = HttpClientUtil.sendPostRaw(notifyUrl, params, "UTF-8");
                        log.info("-------------------{}:订单号发送给下游回调的反回值:{}-------------------", platformOrderNo, result);
                    }
                }.start();
            }
        }

        return new Result(true, platformOrderNo + "通知收到!");
    }

    public String xiayou_notify_res(String resultJSONString) {

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);

        Map<String, String> parmasMap = new HashMap<>(6);
        parmasMap.put("command", jsonObject.getString("command"));
        parmasMap.put("channel", jsonObject.getString("channel"));
        parmasMap.put("payType", jsonObject.getString("payType"));
        parmasMap.put("platformOrderNo", jsonObject.getString("platformOrderNo"));
        parmasMap.put("amount", jsonObject.getString("amount"));
        parmasMap.put("result", jsonObject.getString("result"));
        parmasMap.put("sign", jsonObject.getString("sign"));

        boolean isvalue = SignUtil.verifySign(parmasMap, key);
        if (!isvalue) {
            log.info("结果错误:fail");
            return "fail";
        }

        if (jsonObject.getString("result").equals("OK")) {
            log.info("结果正确:success");
            return "success";
        }

        return null;
    }

    @Transactional
    public void update(String clientOrderNo) {

        PayOrder payOrder = payOrderMapper.getOrderForClientOrderNo(clientOrderNo);
        if (null != payOrder) {
            payOrder.setStatus("7");
            payOrderMapper.updateByPrimaryKeySelective(payOrder);
            log.info("{}:记录更新!", clientOrderNo);
        }
    }
}
