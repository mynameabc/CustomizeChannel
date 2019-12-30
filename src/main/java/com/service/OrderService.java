package com.service;

import com.ClientUserHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mapper.ClientUserMapper;
import com.mapper.GoodsMapper;
import com.mapper.OrderMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderService {

/*    static Logger logger = LoggerFactory.getLogger(OrderService.class);*/

    @Autowired
    private ClientUserHandler clientUserHandler;

    @Autowired
    private ClientUserMapper clientUserMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private String key = "52A1B74DDAFC4274992E51DDCDFCCD9F";

    /**
     * 下单(自己四方调)
     * @param orderDTO
     * @return
     */
    public Result pay(OrderDTO orderDTO) {

        //验签

        //金额是否正确
        log.info("订单数据:{}", orderDTO);
        Goods goods = goodsMapper.getGoodsForOrderAmount(orderDTO.getOrderAmount());
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

        //判断是否有可用链接
        boolean isvalue = WebSocket.isExist();
        if (isvalue == false) {
            log.info("{}:没有可用联接, 请和管理员联系", orderDTO.getPlatformOrderNo());
            return new Result(false, "没有可用联接, 请和管理员联系!");
        }
/*
        //再判断是否有可下单的小号
        int orderNumberCount = 20;
        List<ClientUser> clientUserList = clientUserMapper.getClientUserForNumber(orderNumberCount);
        if (clientUserList.size() <= 0) {
            log.info("{}:下单账号不足, 请和管理员联系!", orderDTO.getPlatformOrderNo());
            return new Result(false, "下单账号不足, 请和管理员联系!");
        }
*/
        //轮询选出账号
        Client client = this.userNamePollSelect();

        SortedMap<String, String> params = WebSocketSendObject.sendObjectForSortedMap("4");

        {
            params.put("channel", orderDTO.getChannel());
            params.put("pay_type", orderDTO.getPayType());
            params.put("amount", orderDTO.getOrderAmount());
            params.put("platform_order_no", orderDTO.getPlatformOrderNo());
            params.put("goods_url", goods.getUrl());
            params.put("user_name", client.getPlaceOrderName());
            params.put("password", client.getPlaceOrderPassword());
            params.put("notify_url", orderDTO.getNotifyUrl());
        }

        String sign = SignUtil.sign(params, key);   //加签
        params.put("sign", sign);

        log.info("发送WebSocket请求给:{}", client.getClientUserName());

        //发送WebSocket请求
        WebSocket.sendMessage(client.getClientUserName(), JSON.toJSONString(params));

        String redisPlatformOrderNoKey = orderDTO.getPlatformOrderNo(); //以平台订单号为key
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setPlatformOrderNo(orderDTO.getPlatformOrderNo());
        orderInfo.setPayUrl("N");

        try {
            redisTemplate.opsForValue().set(redisPlatformOrderNoKey, orderInfo, 5, TimeUnit.MINUTES); //放入缓存, 时间为3分钟过期
        } catch (Exception e) {
            String logInfo = orderDTO.getPlatformOrderNo() + ":redis存放异常!" + "\n";
            logInfo += "[异常信息]  - " + e.toString() + "\n";
            log.error(logInfo);
        }

        log.info("开始监听:{}", orderDTO.getPlatformOrderNo());

        long sleep = 0L;
        for (int index = 1; index <= 6; index++) {

            sleep = 1000 * index;

            log.info("{}---第:{}循环:", orderDTO.getPlatformOrderNo(), index);

            try {
                Thread.sleep(sleep);
            } catch (Exception e) {}

            try {
                orderInfo = (OrderInfo) redisTemplate.opsForValue().get(redisPlatformOrderNoKey);
            } catch (Exception e) {
                String logInfo = orderDTO.getPlatformOrderNo() + ":第" + index + "次redis取出异常!" + "\n";
                logInfo += "[异常信息]  - " + e.toString() + "\n";
                log.error(logInfo);
            }

            if (!StringUtils.isBlank(orderInfo.getClientOrderStatus())) {
                break;
            }
        }

        if (null != orderInfo) {
            if (!orderInfo.getPayUrl().equals("N")) {
                redisTemplate.delete(redisPlatformOrderNoKey);
                return new Result(true, orderInfo.getPayUrl());
            } else {
                redisTemplate.delete(redisPlatformOrderNoKey);
                return new Result(false, "请求超时!");
            }
        } else {
            redisTemplate.delete(redisPlatformOrderNoKey);
            return new Result(false, "下单异常, 请和管理员联系!");
        }
/*
        payOrder = this.getOrderForPlatformOrderNo(orderDTO.getPlatformOrderNo());
        if (null == payOrder) {
            log.error("订单号:{} --------- payOrder记录不存在!", orderDTO.getPlatformOrderNo());
            return new Result(false, "订单请求超时!");
        } else {

            String clientOrderStatus = payOrder.getClientOrderStatus();
            switch (clientOrderStatus)
            {
                case "0":   //成功

                    try {
                        payOrder.setStatus("2");        //成功(支付链接有返回给调用方)
                        payOrder.setUpdateTime(new Date());
                        orderMapper.updateByPrimaryKey(payOrder);
                        log.info("订单号:{} --------- orderInfo对象的ClientOrderStatus={}!", orderDTO.getPlatformOrderNo(), orderInfo.getClientOrderStatus());
                    } catch (Exception e) {
                        String logInfo = orderDTO.getPlatformOrderNo() + ":订单号:{}---记录更新异常!" + "\n";
                        logInfo += "[异常信息]  - " + e.toString() + "\n";
                        log.error(logInfo, orderDTO.getPlatformOrderNo());
                    }

                    params.remove("goods_url");
                    params.remove("user_name");
                    params.remove("password");
                    params.remove("client_order_status");
                    params.put("pay_url", payOrder.getPayOrderUrl());
                    return new Result(true, "订单创建成功", params);
                case "1":   //库存不足
                    log.error("订单号:{}---库存不足!", orderDTO.getPlatformOrderNo());
                    return new Result(false, "库存不足!");
                case "2":   //账号次数达到上限
                    log.error("订单号:{}---账号购买次数达到上限!", orderDTO.getPlatformOrderNo());
                    return new Result(false, "账号购买次数达到上限!");
                case "9":
                    log.error("订单号:{}出现未知异常, 前线返回的客户端状态为空!", orderDTO.getPlatformOrderNo());
                    return new Result(false, "订单超时!");
                default:
                    break;
            }
        }
*/
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

        String user_name = jsonObject.getString("user_name");                       //下单小号
        String pay_type = jsonObject.getString("pay_type");                         //支付方式
        String amount = jsonObject.getString("amount");                             //订单金额
        String clientOrderStatus = jsonObject.getString("client_order_status");     //客户端执行状态 0:成功, 1:库存不足, 2:账号次数达到上限
        String pay_url = jsonObject.getString("pay_url");                           //支付地址
        String client_order_no = jsonObject.getString("client_order_no");           //国美订单号
        String platformOrderNo = jsonObject.getString("platform_order_no");         //平台订单号
        String notify_url = jsonObject.getString("notify_url");                     //回调地址

        try {

            //取出缓存对象判断是否存在
            {
                OrderInfo orderInfo = (OrderInfo) redisTemplate.opsForValue().get(platformOrderNo);
                if (null == orderInfo) {
                    log.error("订单号:{}---该平台订单号不存在!---redis", platformOrderNo);
                    return new Result(false, "该平台订单号不存在");
                }

                if (StringUtils.isBlank(clientOrderStatus)) {
                    orderInfo.setClientOrderStatus("9");        //客户端状态返回空时给于一个状态
                } else {
                    orderInfo.setClientOrderStatus(clientOrderStatus);
                }

                orderInfo.setPayUrl(pay_url);

                redisTemplate.opsForValue().set(platformOrderNo, orderInfo);
            }

            //查看数据库是否存在该订单
            PayOrder payOrder = orderMapper.getOrderForPlatformOrderNo(platformOrderNo);
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
                    payOrder.setPayType(pay_type);
                    payOrder.setPayAmount(amount);
                    payOrder.setOrderAmount(amount);
                    payOrder.setPlatformOrderNo(platformOrderNo);
                    payOrder.setUserName(user_name);
                    payOrder.setClientOrderNo(client_order_no);
                    payOrder.setPayOrderUrl(pay_url);
                    payOrder.setClientOrderStatus(clientOrderStatus);
                    payOrder.setStatus(status);
                    payOrder.setNotifyUrl(notify_url);
//                    payOrder.setNotifyPar(jsonObject.toJSONString());
                    payOrder.setNotifySendNotifyCount(0);
//                    payOrder.setNotifyLastSendTime();
//                    payOrder.setReturnResult("");
                    payOrder.setCreateTime(nowDate);
                    orderMapper.insert(payOrder);

                    ClientUser clientUser = clientUserMapper.getClientUserForName(user_name);
                    if (null == clientUser) {
                        log.error("订单号:{}---该下单小号不存在!", platformOrderNo);
                        return new Result(false, "该下单小号不存在!");
                    }

                    int number = clientUser.getNumber();
                    clientUser.setNumber(++number);
                    clientUserMapper.updateByPrimaryKeySelective(clientUser);
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
        return orderMapper.selectOne(payOrder);
    }

    /**
     * 通知成功回调
     * @param resultJSONString
     * @return
     */
    public Result notify(String resultJSONString) {

        JSONObject jsonObject = JSONObject.parseObject(resultJSONString);

        String user_name = jsonObject.getString("user_name");
        String notifyUrl = jsonObject.getString("notify_url");
        String platformOrderNo = jsonObject.getString("platform_order_no");

        ClientUser clientUser = clientUserMapper.getClientUserForName(user_name);
        int number = clientUser.getNumber();

        //判断小号是否下满
        {
            int orderNumberCount = 20;
            if (number >= orderNumberCount) {

                //选出新的可用下单小号, 并让客户端重新登陆小号
                WebSocket.againLogin(clientUser.getClientName());
            }
        }

        //判断记录是否存在并且pay_order表状态是否是5
        PayOrder payOrder = orderMapper.getOrderForPlatformOrderNo(platformOrderNo);
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
                count = orderMapper.updateByPrimaryKey(payOrder);
            } catch (Exception e) {
                String logInfo = platformOrderNo + ":订单号:{}---更新异常!" + "\n";
                logInfo += "[异常信息]  - " + e.toString() + "\n";
                log.error(logInfo, platformOrderNo);
            }

            SortedMap<String, String> params = new TreeMap<>();
            params.put("command", "4");         //0:心跳, 1:登陆, 2:小号登陆失败, 3:小号登陆成功, 4:下单
            params.put("channel", "");

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

        log.info("-------------------{}订单号收到通知!-------------------", platformOrderNo);
        log.info("-------------------{}订单号回调通知参数-------------------", jsonObject);

        return new Result(true, platformOrderNo + "通知收到!");
    }

    /**
     * 轮询选出账号
     * @return
     */
    public Client userNamePollSelect() {
        return WebSocket.getWebSocketClientUserName();
    }
}
