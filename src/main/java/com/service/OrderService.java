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

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderService {

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

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 下单(自己四方调)
     *
     * @param orderDTO
     * @return
     */
    public Result pay(OrderDTO orderDTO) {

        //金额是否正确
        Goods goods = (Goods)redissonClient
                .getBucket(ProjectConstant.GOODS_KEY_PAY_AMOUNT + orderDTO.getAmount()).get();
        if (null == goods) {
            log.warn("{}:不支持该金额", orderDTO.getPlatformOrderNo());
            return new Result(false, "不支持该金额!");
        }

        //订单号是否存在
        PayOrder payOrder = this.getOrderForPlatformOrderNo(orderDTO.getPlatformOrderNo());
        if (null != payOrder) {
            log.warn("{}:该平台订单已存在, 请不要重复下单.", orderDTO.getPlatformOrderNo());
            return new Result(false, "该平台订单已存在, 请不要重复下单!");
        }

        //轮询选出账号
        List<Client> list = WebSocket.getWebSocketUsablePlaceOrderList();
        if (list.isEmpty()) {
            log.warn("{}:没有可用下单小号!", orderDTO.getPlatformOrderNo());
            return new Result(false, "没有可用下单小号!");
        }
        Client client = roundRobin.getClient(list);
        if (null == client) {
            log.warn("{}:没有可用下单小号!", orderDTO.getPlatformOrderNo());
            return new Result(false, "没有可用下单小号!");
        }

        //以平台订单号为key
        String platformOrderNoKey = orderDTO.getPlatformOrderNo();

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setPayUrl(ProjectConstant.FAIL);
        orderInfo.setClientOrderStatus("");

        RBucket<OrderInfo> orderInfoBucket = redissonClient.getBucket(ProjectConstant.PAY_ORDER_KEY + platformOrderNoKey);
        orderInfoBucket.set(orderInfo, 1, TimeUnit.MINUTES);

        {
            SortedMap<String, String> params = WebSocketSendObject.sendObjectForSortedMap("4");

            params.put("channel", orderDTO.getChannel());
            params.put("pay_type", orderDTO.getPayType());
            params.put("amount", orderDTO.getAmount());
            params.put("platform_order_no", orderDTO.getPlatformOrderNo());
            params.put("goods_url", goods.getUrl());
            params.put("user_name", client.getPlaceOrderName());
            params.put("password", client.getPlaceOrderPassword());
            params.put("notify_url", orderDTO.getNotifyUrl());

            //发送WebSocket请求
            WebSocket.sendMessage(client.getClientUserName(), JSON.toJSONString(params));
            log.info("WebSocket下单ID是:{} ------ 数据是:{}", client.getClientUserName(), params.toString());
        }

        log.info("开始监听:{}", orderDTO.getPlatformOrderNo());

        long sleep;
        int numberTimes = 6;
        for (int index = 1; index <= numberTimes; index++) {
            log.info("{}---第:{}循环:", orderDTO.getPlatformOrderNo(), index);
            sleep = 1000 * index;
            try {Thread.sleep(sleep);} catch (Exception ignored) {}
            orderInfo = orderInfoBucket.get();
            if (!StringUtils.isBlank(orderInfo.getClientOrderStatus())) {break;}
        }

        if (!orderInfo.getPayUrl().equals(ProjectConstant.FAIL)) {
            orderInfoBucket.delete();
            return new Result(true, "成功!", orderInfo.getPayUrl());
        } else {
            orderInfoBucket.delete();
            return new Result(false, "请求超时!");
        }
    }

    /**
     * 设置支付URL(前线调)
     *
     * @param resultJSONString
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result setPayUrl(String resultJSONString) {

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);

        //商品URL
        String goods_url = jsonObject.getString("goods_url");
        //充值渠道
        String channel = jsonObject.getString("channel");
        //下单小号
        String user_name = jsonObject.getString("user_name");
        //下单小号密码
        String password = jsonObject.getString("password");
        //支付方式
        String pay_type = jsonObject.getString("pay_type");
        //订单金额
        String amount = jsonObject.getString("amount");
        //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String clientOrderStatus = jsonObject.getString("client_order_status");
        //支付地址
        String pay_url = jsonObject.getString("pay_url");
        //国美订单号
        String client_order_no = jsonObject.getString("client_order_no");
        //平台订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");
        //回调地址
        String notify_url = jsonObject.getString("notify_url");
        //加签后字符串
        String sign = jsonObject.getString("sign");

        String key = systemConfigService.getStringValue(ProjectConstant.PRIVATE_KEY);

        String jsonString = "amount=" + amount + "channel=" + channel + "clientOrderStatus="
                + clientOrderStatus + "platformOrderNo=" + platformOrderNo + "key=" + key;
        String _sign = MD5Util.MD5(jsonString).toUpperCase();
        if (!_sign.equals(sign)) {
            log.error("setPayURL方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "setPayURL参数错误!");
        }

        try {

            //取出缓存对象判断是否存在
            {
                RBucket<OrderInfo> rBucket = redissonClient.getBucket(ProjectConstant.PAY_ORDER_KEY + platformOrderNo);
                OrderInfo orderInfo = rBucket.get();

                if (null == orderInfo) {
                    log.error("订单号:{}---该平台订单号不存在!---redis", platformOrderNo);
                    return new Result(false, "该平台订单号不存在");
                }

                if (StringUtils.isBlank(clientOrderStatus)) {
                    //客户端状态返回空时给于一个状态
                    orderInfo.setClientOrderStatus("9");
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
                            int orderNumberCount = systemConfigService.getIntegerValue(ProjectConstant.SUPERIOR_LIMIT).intValue();
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

                rBucket.set(orderInfo, 30, TimeUnit.SECONDS);
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

                switch (clientOrderStatus) {
                    case "0":
                        //支付链接生成成功
                        status = "1";
                        break;
                    case "1":
                        //支付连接生成失败
                        status = "3";
                        break;
                    case "2":
                        //支付连接生成失败
                        status = "3";
                        break;
                    case "9":
                        //前线返回未知状态
                        status = "6";
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
     *
     * @param resultJSONString
     * @return
     */
    public Result notify(String resultJSONString) {

        log.info("notify方法中---{}", resultJSONString);

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);

        String notifyUrl = jsonObject.getString("notify_url");

        //加签后字符串
        String sign = jsonObject.getString("sign");
        //商品URL
        String goods_url = jsonObject.getString("goods_url");
        //充值渠道
        String channel = jsonObject.getString("channel");
        //下单小号
        String user_name = jsonObject.getString("user_name");
        //下单小号密码
        String password = jsonObject.getString("password");
        //支付方式
        String pay_type = jsonObject.getString("pay_type");
        //订单金额
        String amount = jsonObject.getString("amount");
        //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String clientOrderStatus = jsonObject.getString("client_order_status");
        //支付地址
        String pay_url = jsonObject.getString("pay_url");
        //国美订单号
        String client_order_no = jsonObject.getString("client_order_no");
        //平台订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");

        String key = systemConfigService.getStringValue(ProjectConstant.PRIVATE_KEY);
        String _temp = "amount=" + amount + "channel=" + channel + "clientOrderStatus="
                + clientOrderStatus + "platformOrderNo=" + platformOrderNo + "key=" + key;
        String _sign = MD5Util.MD5(_temp).toUpperCase();
        if (!_sign.equals(sign)) {
            log.error("notify方法中---{}:该订单验签没通过!---{}", platformOrderNo, jsonObject.toString());
            return new Result(false, "notify参数错误!");
        }

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

            //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单, 5:发送收货信号, 6, 确认收货
            params.put("command", jsonObject.getString("command"));
            params.put("channel", jsonObject.getString("channel"));
            params.put("payType", jsonObject.getString("pay_type"));
            params.put("platformOrderNo", jsonObject.getString("platform_order_no"));
            params.put("amount", jsonObject.getString("amount"));
            params.put("result", "OK");
            String xsign = SignUtil.sign(params, key);
            params.put("sign", xsign);

            log.info("{}:发送给下游的参数:{}", params.get("platformOrderNo"), params);

            //更新成功
            if (count >= 1) {
                new Thread(() -> {
                    String result = HttpClientUtil.sendPostRaw(notifyUrl, params, "UTF-8");
                    log.info("-------------------{}:订单号发送给下游回调的反回值:{}-------------------", platformOrderNo, result);
                }).start();
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

        String key = systemConfigService.getStringValue(ProjectConstant.PRIVATE_KEY);
        boolean isValue = SignUtil.verifySign(parmasMap, key);
        if (!isValue) {
            log.info("结果错误:fail");
            return "fail";
        }

        if (jsonObject.getString("result").equals("OK")) {
            log.info("结果正确:success");
            return "success";
        }

        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(String clientOrderNo) {

        PayOrder payOrder = payOrderMapper.getOrderForClientOrderNo(clientOrderNo);
        if (null != payOrder) {
            payOrder.setStatus("7");
            payOrderMapper.updateByPrimaryKeySelective(payOrder);
            log.info("{}:记录更新!", clientOrderNo);
        }
    }
}
