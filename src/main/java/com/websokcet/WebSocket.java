package com.websokcet;

import com.ClientUserHandler;
import com.auxiliary.test.NormalRoundRobinWebSocketImpl;
import com.pojo.customize.Client;
import com.pojo.customize.ClientUserInfo;
import com.pojo.entity.ClientUser;
import com.service.OrderService;
import com.utils.WebSocketSendObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Administrator
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{userName}")
public class WebSocket {

    /**
     * 标识
     */
    private String userName = "";

    /**
     * 静态变量, 用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * 与某个客户端的连接会话, 需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 根据userName来获取对应的WebSocket
     */
    private static ConcurrentHashMap<String, Client> websocketMap = new ConcurrentHashMap<>();

    public static NormalRoundRobinWebSocketImpl normalRoundRobinWebSocketImpl;

    public static ClientUserHandler clientUserHandler;

    public static OrderService orderService;

    /**
     * 当建立链接的时候调用此方法
     * @param userName 代表地址参数中的name 用于区分链接是谁
     * @param session 当前建立的链接
     */
    @OnOpen
    public void onOpen(@PathParam("userName") String userName, Session session) {

        this.session = session;
        this.userName = userName;

        int maxSize = 200 * 1024;

        //可以缓冲的传入二进制消息的最大长度
        this.session.setMaxBinaryMessageBufferSize(maxSize);
        //可以缓冲的传入文本消息的最大长度
        this.session.setMaxTextMessageBufferSize(maxSize);

        Object lock = 0;
        synchronized (lock) {

            if (websocketMap.containsKey(userName)) {
                log.info("该账号已链接!");
                return;
            } else {

                //判断websocket客户端ID是否有绑定的小号, 是则不发登陆信息, 否则发
                ClientUserInfo clientUserInfo = clientUserHandler.getClientUser(userName);
                if (null != clientUserInfo) {

                    //把对象初始化下扔进 websocketMap
                    Client client = new Client();
                    client.setWebSocket(this);

                    //客户端连接标识
                    client.setClientUserName(userName);
                    //下单小号
                    client.setPlaceOrderName(clientUserInfo.getClientUser().getName());
                    //下单密码
                    client.setPlaceOrderPassword(clientUserInfo.getClientUser().getPassword());
                    //是否可以下单
                    client.setPlaceOrderStatus(clientUserInfo.getPlaceOrderStatus());

                    //如果是新建的连接, 就发送消息
                    if (clientUserInfo.getStatus() == 1) {

                        //如果是新连接则未登陆
                        client.setLoginStatus(0);
                        websocketMap.put(userName, client);
                        //给客户端发送消息(下单小号和密码)
                        {
                            JSONObject jsonObject = WebSocketSendObject.sendObjectForJSONObject("1");

                            //下单小号
                            jsonObject.put("user_name", clientUserInfo.getClientUser().getName());
                            //下单小号密码
                            jsonObject.put("password", clientUserInfo.getClientUser().getPassword());
                            //IP
                            jsonObject.put("proxy_ip", clientUserInfo.getClientUser().getProxyIp());
                            //ip密码
                            jsonObject.put("proxy_port", clientUserInfo.getClientUser().getProxyPort());
                            //账号
                            jsonObject.put("proxy_user", clientUserInfo.getClientUser().getProxyUser());
                            //密码
                            jsonObject.put("proxy_psw", clientUserInfo.getClientUser().getProxyPsw());
                            log.info("给---{}---发送登陆通知!", userName);
                            websocketMap.get(userName).getWebSocket().session.getAsyncRemote().sendText(jsonObject.toJSONString());
                        }

                        //建立联接
                        {
                            clientUserHandler.connect(userName, clientUserInfo.getClientUser().getName());
                        }

                        addOnlineCount();
                        log.info("{}----------------------------新连接, 连接数:{}", userName, getOnlineCount());

                    } else {
                        //如果是旧连接则已登陆
                        client.setLoginStatus(1);
                        websocketMap.put(userName, client);
                        addOnlineCount();
                        //把登陆状态更新到数据库
                        clientUserHandler.login(userName);
                        log.info("{}----------------------------旧连接, 连接数:{}", userName, getOnlineCount());
                    }

                } else {
                    log.info("没有小号分配了!");
                }
            }
        }
    }

    /**
     * 测试用 单个连接登陆
     * @param toUserName
     */
    public static void testSingleLogin(String toUserName) {

        ClientUser clientUser = clientUserHandler.getClientUser();

        //建立联接
        {
            //先解除
            clientUserHandler.removeRelation(toUserName);
            //连接
            clientUserHandler.connect(toUserName, clientUser.getName());
        }

        //给客户端发送消息
        JSONObject jsonObject = WebSocketSendObject.sendObjectForJSONObject("1");
        //下单小号
        jsonObject.put("user_name", clientUser.getName());
        //下单小号密码
        jsonObject.put("password", clientUser.getPassword());
        //IP
        jsonObject.put("proxy_ip", clientUser.getProxyIp());
        //ip密码
        jsonObject.put("proxy_port", clientUser.getProxyPort());
        //账号
        jsonObject.put("proxy_user", clientUser.getProxyUser());
        //密码
        jsonObject.put("proxy_psw", clientUser.getProxyPsw());
        log.info("给---{}---发送登陆通知!", toUserName);
        websocketMap.get(toUserName).getWebSocket().session.getAsyncRemote().sendText(jsonObject.toJSONString());
    }

    /**
     * 用来接收客户端发来的消息, 这个地方应该根据自己的实际业务需求, 来决定到底写什么
     * @param userName
     * @param message
     */
    @OnMessage
    public void onMessage(@PathParam("userName") String userName, String message) {

        log.info("服务器收到:" + message);

        JSONObject jsonObject = null;

        try {
            jsonObject = JSONObject.parseObject(message);
        } catch (Exception e) {
            log.error("JSON解析异常!{}", message);
        }

        String command = jsonObject.getString("command");

        //登陆 服务器发送到客户端
        if (command.equals("1")) {
            //配发下单小号给客户端连接
        }

        //小号登陆失败 客户端发送服务器
        if (command.equals("2")) {
            //重新配发下单小号给客户端连接
        }

        //小号登陆成功更新状态 <客户端发送服务器>
        if (command.equals("3")) {

            log.info("{}:已登陆成功!", userName);

            Client client = websocketMap.get(userName);
            client.setLoginStatus(1);
            websocketMap.put(userName, client);
            clientUserHandler.login(websocketMap.get(userName).getClientUserName());
        }

        //收货, 改变数据库状态
        if (command.equals("6")) {

            String client_order_no = jsonObject.getString("client_order_no");
            log.info("{}:开始确认收货---订单号是:{}", userName, client_order_no);
            orderService.receivingGoodsStatusYes(client_order_no);
        }
    }

    /**
     * 当关闭链接触发
     */
    @OnClose
    public void OnClose(@PathParam("userName") String userName, Session session) {

        if (websocketMap.containsKey(userName)) {
            clientUserHandler.removeRelation(userName);
            subOnlineCount();
            websocketMap.remove(userName);
            log.info("{}:连接关闭！当前在线人数为:", userName, getOnlineCount());
        }
    }

    /**
     * 关闭单个联接
     * @param userName
     */
    public static void singleClose(String userName) {
        if (websocketMap.containsKey(userName)) {
            clientUserHandler.removeRelation(userName);
            subOnlineCount();
            websocketMap.remove(userName);
            log.info("{}:连接关闭！当前在线人数为:", userName, getOnlineCount());
        }
    }

    /**
     * 当出现异常时触发
     * @param userName
     * @param throwable
     * @param session
     */
    @OnError
    public void OnError(@PathParam("userName") String userName, Throwable throwable, Session session) {
        log.info("账号:{} --- 出现异常:---{}", userName, throwable.getMessage());
        OnClose(userName, session);
    }

    /**
     * 实现服务器主动推送给指定客户端
     * @param toUserName
     * @param message
     */
    public static void sendMessage(String toUserName, String message) {
        if (websocketMap.containsKey(toUserName)) {
            websocketMap.get(toUserName).getWebSocket().session.getAsyncRemote().sendText(message);
            log.info("发给账号:" + toUserName + "---" + message);
        } else {
            log.info("{}:该链接不存在!", toUserName);
        }
    }

    /**
     * //考虑用并发搞
     * 给客户端发送心跳
     * @param message
     * @throws IOException
     */
    public static synchronized void sendPing(String message) {

        Iterator entries = websocketMap.entrySet().iterator();
        while (entries.hasNext()) {

            Map.Entry entry = (Map.Entry) entries.next();
            websocketMap.get(entry.getKey()).getWebSocket().session.getAsyncRemote().sendText(message);
            log.info("给---{}---发送心跳!", entry.getKey());
        }
    }

    /**
     * 返回当前链接数
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 添加链接数
     */
    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    /**
     * 减除链接数
     */
    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    public static ConcurrentHashMap getWebsocketMap() {
        return websocketMap;
    }

    public static Client getClient(String userName) {
        return websocketMap.get(userName);
    }

    public static synchronized void setClient(Client client) {
        websocketMap.put(client.getClientUserName(), client);
    }

    public static List<Client> getWebSocketUsablePlaceOrderList() {

        Map<String, Client> collect = websocketMap.entrySet().stream()
                .filter(map -> map.getValue().getLoginStatus() == 1)
                .filter(map -> map.getValue().getPlaceOrderStatus() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Client> list = new ArrayList<>(collect.values());
        return list;
    }
}
