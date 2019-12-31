package com.websokcet;

import com.ClientUserHandler;
import com.pojo.customize.Client;
import com.pojo.entity.ClientUser;
import com.utils.ApplicationContextRegister;
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
 * websocket是多例对象
 */

@Slf4j
@Component
@ServerEndpoint("/websocket/{userName}")
public class WebSocket {

    //接收
    private String userName = "";

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //根据userName来获取对应的WebSocket
    private static ConcurrentHashMap<String, Client> websocketMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Integer> pollSelectValue = new ConcurrentHashMap<>();

    public WebSocket() {
        pollSelectValue.put("value", 0);
    }

    /**
     * 当建立链接的时候调用此方法
     * @param userName 代表地址参数中的name 用于区分链接是谁
     * @param session 当前建立的链接
     */
    @OnOpen
    public void onOpen(@PathParam("userName") String userName, Session session) {

        this.session = session;
        this.userName = userName;

        Object lock = 0;
        synchronized (lock) {

            if (websocketMap.containsKey(userName)) {
                log.info("该账号已链接!");
                return;
            } else {

                ClientUserHandler clientUserHandler = ApplicationContextRegister.getApplicationContext().getBean(ClientUserHandler.class);
                ClientUser clientUser = clientUserHandler.againLogin(userName);
                if (null != clientUser) {

                    //把对象初始化下扔进 websocketMap
                    {
                        Client client = new Client();
                        client.setWebSocket(this);
                        client.setPlaceOrderLoginStatus(0);                         //未登陆
                        client.setClientUserName(userName);                         //客户端连接标识
                        client.setPlaceOrderName(clientUser.getName());             //下单小号
                        client.setPlaceOrderPassword(clientUser.getPassword());     //下单密码
                        websocketMap.put(userName, client);
                    }

                    //给客户端发送消息(下单小号和密码)
                    {
                        JSONObject jsonObject = WebSocketSendObject.sendObjectForJSONObject("1");
                        jsonObject.put("user_name", clientUser.getName());            //下单小号
                        jsonObject.put("password", clientUser.getPassword());             //下单小号密码
                        websocketMap.get(userName).getWebSocket().session.getAsyncRemote().sendText(jsonObject.toJSONString());
                    }

                    addOnlineCount();
                    log.info(userName + ":链接被建立!, 当前在线人数为:" + getOnlineCount() + "---下单小号:" + clientUser.getName());

                } else {

                    Client client = new Client();
                    client.setWebSocket(this);
                    client.setPlaceOrderLoginStatus(0);   //未登陆
                    client.setClientUserName(userName);   //客户端连接标识
                    client.setPlaceOrderName("");         //下单小号
                    client.setPlaceOrderPassword("");     //下单密码
                    websocketMap.put(userName, client);

                    addOnlineCount();
                    log.info(userName + ":链接被建立!, 当前在线人数为:" + getOnlineCount() + "---无下单小号分配:");
                }
            }
        }
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
        if (command.equals("1")) {  //登陆 服务器发送到客户端
            //配发下单小号给客户端连接
        }

        if (command.equals("2")) {  //小号登陆失败 客户端发送服务器
            //重新配发下单小号给客户端连接
        }

        if (command.equals("3")) {  //小号登陆成功更新状态 <客户端发送服务器>

            log.info("{}:已登陆成功!", userName);

            Client client = websocketMap.get(userName);
            client.setPlaceOrderLoginStatus(1);
            websocketMap.put(userName, client);
        }

        if (command.equals("7")) {

            log.info("{}:开始确认收货!", userName);
            String client_order_no = jsonObject.getString("client_order_no");
            //更新payOrder表status状态为7
        }
    }

    /**
     * 当关闭链接触发
     */
    @OnClose
    public void OnClose(@PathParam("userName") String userName, Session session) {

        if (websocketMap.containsKey(userName)) {
            ClientUserHandler clientUserHandler = ApplicationContextRegister.getApplicationContext().getBean(ClientUserHandler.class);
            clientUserHandler.removeRelation(userName);
            subOnlineCount();
            websocketMap.remove(userName);
            log.info("有一连接关闭！当前在线人数为:" + getOnlineCount());
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

        log.info("出现异常!---{}", userName);
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
     * 通知websocket账号再登陆
     * @param toUserName websocket账号
     * @return
     */
    public static ClientUser againLogin(String toUserName) {

        ClientUser clientUser = null;

        Object lock = 0;
        synchronized (lock) {

            if (websocketMap.containsKey(toUserName)) {

                ClientUserHandler clientUserHandler = ApplicationContextRegister.getApplicationContext().getBean(ClientUserHandler.class);
                clientUser = clientUserHandler.againLogin(toUserName);
                if (null != clientUser) {

                    Client client = websocketMap.get(toUserName);
                    client.setPlaceOrderLoginStatus(1);
                    client.setPlaceOrderName(clientUser.getName());
                    client.setPlaceOrderPassword(clientUser.getPassword());
                    websocketMap.put(toUserName, client);

                    //给客户端发送消息(下单小号和密码)
                    {
                        JSONObject jsonObject = WebSocketSendObject.sendObjectForJSONObject("1");
                        jsonObject.put("user_name", clientUser.getName());            //下单小号
                        jsonObject.put("password", clientUser.getPassword());         //下单小号密码
                        log.info(jsonObject.toJSONString());
                        websocketMap.get(toUserName).getWebSocket().session.getAsyncRemote().sendText(jsonObject.toJSONString());
                    }

                    log.info("{}再次登陆---新下单小号:{}",toUserName, clientUser.getName());
                } else {

                    Client client = websocketMap.get(toUserName);
                    client.setPlaceOrderLoginStatus(0);
                    client.setPlaceOrderName(clientUser.getName());
                    client.setPlaceOrderPassword(clientUser.getPassword());
                    websocketMap.put(toUserName, client);
                }

            } else {
                log.info("没有找到:{}这个WebSocket链接!", toUserName);
            }
        }

        return clientUser;
    }

    /**
     * 心跳
     * @param message
     * @throws IOException
     */
    public synchronized void sendPing(String message) throws IOException {

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

    /**
     * 是否存在可下单的websocket联接
     * @return
     */
    public static boolean isExistPlaceOrderLoginStatus() {

        Map<String, Client> collect = websocketMap.entrySet().stream()
                .filter(map -> map.getValue().getPlaceOrderLoginStatus() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return (collect.size() >= 1) ? (true) : (false);
    }

    public static Client getWebSocketClientUserName() {

        List<Client> list = new ArrayList<>(websocketMap.values());
        List<Client> tempList = list.stream().filter(Client->(Client.getPlaceOrderLoginStatus() == 1)).collect(Collectors.toList());

        Random random = new Random();
        int index = random.nextInt(tempList.size());

        return tempList.get(index);
    }
}
