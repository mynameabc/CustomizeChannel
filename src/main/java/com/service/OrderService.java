package com.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auxiliary.RoundRobin;
import com.auxiliary.constant.ClientUserContant;
import com.auxiliary.constant.OrderClientContant;
import com.auxiliary.constant.OrderContant;
import com.auxiliary.constant.ProjectConstant;
import com.mapper.ClientUserMapper;
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
    private RedissonClient redissonClient;

    @Autowired
    private RoundRobin roundRobin;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SnowflakeIdUtils snowflakeIdUtils;

    /**
     * 下单(自己四方调)
     *
     * @param orderDTO
     * @return
     */
    public Result pay(OrderDTO orderDTO) {

        //金额是否正确
        Goods goods = (Goods) redissonClient.
                getBucket(ProjectConstant.GOODS_KEY_PAY_AMOUNT + orderDTO.getAmount()).get();
        if (null == goods) {
            log.warn("{}:不支持该金额", orderDTO.getPlatformOrderNo());
            return new Result("00004", "不支持该金额!");
        }

        //轮询选出账号
        List<Client> list = WebSocket.getWebSocketUsablePlaceOrderList();
        if (list.isEmpty()) {
            log.warn("{}:没有可用下单小号!", orderDTO.getPlatformOrderNo());
            return new Result("00005", "没有可用下单小号!");
        }
        Client client = roundRobin.getClient(list);
        if (null == client) {
            log.warn("{}:没有可用下单小号!", orderDTO.getPlatformOrderNo());
            return new Result("00005", "没有可用下单小号!");
        }

        //订单号是否存在
        PayOrder payOrder = this.getOrderForPlatformOrderNo(orderDTO.getPlatformOrderNo());
        if (null != payOrder) {
            log.warn("{}:该平台订单已存在, 请不要重复下单.", orderDTO.getPlatformOrderNo());
            return new Result("00006", "该平台订单已存在, 请不要重复下单!");
        }

        OrderInfo orderInfo = new OrderInfo();
        RBucket<OrderInfo> orderInfoBucket =
                redissonClient.getBucket(ProjectConstant.PAY_ORDER_KEY + orderDTO.getPlatformOrderNo());
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
            if (!OrderClientContant.INITIALIZATION.equals(orderInfo.getClientOrderStatus())) {
                break;
            }
        }

        Result result;
        switch (orderInfo.getClientOrderStatus()) {
            case OrderClientContant.SUCCESS:
                log.info("{}:请求成功, 链接地址:{}", orderDTO.getPlatformOrderNo(), orderInfo.getPayUrl());
                result = new Result("00000", true, "请求成功!", orderInfo.getPayUrl());
                break;
            case OrderClientContant.NO_QUANTITY_AVAILABLE_IN_STOCK:
                log.warn("{}:该商品库存不足!", orderDTO.getAmount());
                result = new Result("00007", orderDTO.getAmount() + ":该商品库存不足!");
                break;
            case OrderClientContant.ACCOUNT_NUMBER_REACHES_THE_SUPPER_LIMIT:
                log.warn("{}:小号下单次数达到上限!", orderDTO.getAmount());
                result = new Result("00008", "小号下单次数达到上限!");
                break;
            case OrderClientContant.UNCERTAIN:
                log.warn("{}:客户端返回未知标识, 请和管理员联系!", orderDTO.getPlatformOrderNo());
                result = new Result("00009", "客户端返回未知标识, 请和管理员联系!");
                break;
            default:
                log.warn("{}:请求超时!", orderDTO.getPlatformOrderNo());
                result = new Result("00010", "请求超时!");
                break;
        }
        return result;
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
        String goodsUrl = jsonObject.getString("goods_url");
        //充值渠道
        String channel = jsonObject.getString("channel");
        //下单小号
        String userName = jsonObject.getString("user_name");
        //下单小号密码
        String password = jsonObject.getString("password");
        //支付方式
        String payType = jsonObject.getString("pay_type");
        //订单金额
        String amount = jsonObject.getString("amount");
        //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String clientOrderStatus = jsonObject.getString("client_order_status");
        //支付地址
        String payUrl = jsonObject.getString("pay_url");
        //国美订单号
        String clientOrderNo = jsonObject.getString("client_order_no");
        //平台订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");
        //回调地址
        String notifyUrl = jsonObject.getString("notify_url");
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

        RBucket<OrderInfo> rBucket = redissonClient.getBucket(ProjectConstant.PAY_ORDER_KEY + platformOrderNo);
        OrderInfo orderInfo = rBucket.get();
        if (null == orderInfo) {
            log.error("订单号:{}---该平台订单号不存在!", platformOrderNo);
            return new Result(false, "该平台订单号不存在!");
        } else {

            orderInfo.setPayUrl(payUrl);
            orderInfo.setClientOrderStatus(clientOrderStatus);
            rBucket.set(orderInfo);

            //如果成功返回支付地址
            if (clientOrderStatus.equals(OrderClientContant.SUCCESS)) {

                ClientUser clientUser = clientUserMapper.getClientUserForName(userName);
                Client client = WebSocket.getClient(clientUser.getClientName());
                int number = clientUser.getNumber() + 1;

                clientUser.setNumber(number);
                client.setPlaceOrderNumber(number);

                //判断小号是否下满
                int orderNumberCount = systemConfigService.getIntegerValue(ProjectConstant.SUPERIOR_LIMIT);
                if (number >= orderNumberCount) {
                    client.setPlaceOrderStatus(Integer.parseInt(ClientUserContant.NO));
                    clientUser.setPlaceOrderStatus(ClientUserContant.NO);
                }
                clientUserMapper.updateByPrimaryKeySelective(clientUser);
                WebSocket.setClient(client);
            }
        }

        String status = "";
        if (clientOrderStatus.equals(OrderClientContant.SUCCESS)) {status = OrderContant.SUCCESS;}
        if (clientOrderStatus.equals(OrderClientContant.NO_QUANTITY_AVAILABLE_IN_STOCK)) {status = OrderContant.FAIL;}
        if (clientOrderStatus.equals(OrderClientContant.ACCOUNT_NUMBER_REACHES_THE_SUPPER_LIMIT)) {status = OrderContant.FAIL;}
        if (clientOrderStatus.equals(OrderClientContant.UNCERTAIN)) {clientOrderStatus = OrderClientContant.UNCERTAIN_VALUE;status = OrderContant.FAIL;}

        try {

            //查看数据库是否存在该订单
            PayOrder payOrder = payOrderMapper.getOrderForPlatformOrderNo(platformOrderNo);
            if (null != payOrder) {
                log.error("订单号:{}---该平台订单号已在存!", platformOrderNo);
                return new Result(false, "该平台订单号已在存!");
            }

            payOrder = new PayOrder();
            {
                payOrder.setOrderId(snowflakeIdUtils.nextId());
                payOrder.setSign(sign);
                payOrder.setGoodsUrl(goodsUrl);
                payOrder.setChannel(channel);
                payOrder.setPayType(payType);
                payOrder.setAmount(amount);
                payOrder.setPlatformOrderNo(platformOrderNo);
                payOrder.setUserName(userName);
                payOrder.setPassword(password);
                payOrder.setClientOrderNo(clientOrderNo);
                payOrder.setPayUrl(payUrl);
                payOrder.setClientOrderStatus(clientOrderStatus);
                payOrder.setStatus(status);
                payOrder.setReceivingGoodsStatus(OrderContant.RECEIVING_GOODS_STATUS_NO);
                payOrder.setNotifyUrl(notifyUrl);
                payOrder.setNotifySendNotifyCount(0);
                payOrder.setCreateTime(new Date());
                payOrderMapper.insert(payOrder);
            }

            log.info("-------------------设置支付URL方法成功-------------------:" + clientOrderStatus);

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

        //订单状态(0:支付连接生成成功, 1:支付成功(收到回调), 2:支付连接生成失败)
        if (payOrder.getStatus().equals(OrderContant.PAY_SUCCESS)) {
            log.info("{}:该订单号状态已设为支付成功---回调方法!", platformOrderNo);
            return new Result(false, platformOrderNo + ":已通知过, 不要重复发送!");
        }

        //订单状态是支付连接生成成功时
        if (payOrder.getStatus().equals(OrderContant.SUCCESS)) {

            int count = 0;
            try {
                payOrder.setStatus(OrderContant.PAY_SUCCESS);
                payOrder.setPayTime(new Date());
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

    /**
     * 确认收货
     * @param clientOrderNo
     */
    @Transactional(rollbackFor = Exception.class)
    public void receivingGoodsStatusYes(String clientOrderNo) {
        PayOrder payOrder = payOrderMapper.getOrderForClientOrderNo(clientOrderNo);
        if (null != payOrder) {
            payOrder.setReceivingGoodsStatus(OrderContant.RECEIVING_GOODS_STATUS_YES);
            payOrderMapper.updateByPrimaryKeySelective(payOrder);
            log.info("{}:记录更新!", clientOrderNo);
        }
    }
}
