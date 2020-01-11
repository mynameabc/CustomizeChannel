package com.auxiliary.test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.pojo.customize.Client;
import com.websokcet.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NormalRoundRobinImpl implements INormalRoundRobin {

    private List<Client> servers;

    private int totalServer;
    private int currentIndex;

    private static ConcurrentHashMap<String, Client> websocketMap = new ConcurrentHashMap<>();

    public NormalRoundRobinImpl() {
        init();
    }

    /**
     * 初始化
     */
    @Override
    public void init() {

        servers = new ArrayList<>();
        /*
        servers.add("clientUserName1");
        servers.add("clientUserName2");
        servers.add("clientUserName3");
        servers.add("clientUserName4");
        servers.add("clientUserName5");
        servers.add("clientUserName6");
        servers.add("clientUserName7");
        servers.add("clientUserName8");
        servers.add("clientUserName9");
        servers.add("clientUserName10");
        */

        Client client = new Client();
        client.setPlaceOrderName("1");
        client.setPlaceOrderPassword("1");
        client.setLoginStatus(1);
        client.setClientUserName("aaa");
        websocketMap.put("1", client);

        Client client2 = new Client();
        client2.setPlaceOrderName("2");
        client2.setPlaceOrderPassword("2");
        client2.setLoginStatus(1);
        client2.setClientUserName("bbb");
        websocketMap.put("2", client2);

        Client client3 = new Client();
        client3.setPlaceOrderName("3");
        client3.setPlaceOrderPassword("3");
        client3.setLoginStatus(1);
        client3.setClientUserName("ccc");
        websocketMap.put("3", client3);

        Map<String, Client> collect = websocketMap.entrySet().stream()
                .filter(map -> map.getValue().getLoginStatus() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        servers = new ArrayList<>(collect.values());
        totalServer = servers.size();
        currentIndex = totalServer - 1;
    }

    /**
     * 轮询
     * @return
     */
    @Override
    public Client round() {
        currentIndex = (currentIndex + 1) % totalServer;
        Client client = servers.get(currentIndex);
        log.info("client.getClientUserName(), {}", client.getClientUserName());
        log.info("totalServer------------------------------{}", totalServer);
        log.info("currentIndex------------------------------{}", currentIndex);
        return client;
    }

    public static void main(String[] args) {

        INormalRoundRobin r = new NormalRoundRobinImpl();
        // 不带并发的轮询

        for (int i = 0; i < 14; i++) {
            System.out.println(r.round());
        }
/*
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + r.round());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + r.round());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + r.round());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + r.round());
            }
        }).start();
*/

/*
        for (int i = 0; i < 14; i++) {
            System.out.println(r.round());
        }

        System.out.println();
        System.out.println("==========================");
        System.out.println();

        final CyclicBarrier b = new CyclicBarrier(14);

        // 带并发的轮询
        for (int i = 0; i < 14; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        b.await();
                        System.out.println(Thread.currentThread().getName() + " " + r.round());
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }, "thread" + i).start();
        }
 */
    }
}
