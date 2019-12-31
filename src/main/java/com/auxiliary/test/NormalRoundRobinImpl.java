package com.auxiliary.test;

import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class NormalRoundRobinImpl implements INormalRoundRobin {

    private List<String> servers;

    private int totalServer;
    private int currentIndex;

    public NormalRoundRobinImpl() {
        init();
    }

    /**
     * 初始化
     */
    public void init() {

        servers = new ArrayList<>();
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
        totalServer = servers.size();
        currentIndex = totalServer - 1;
    }

    /**
     * 轮询
     * @return
     */
    public String round() {
        currentIndex = (currentIndex + 1) % totalServer;
        return servers.get(currentIndex);
    }

    public static void main(String[] args) {

        INormalRoundRobin r = new NormalRoundRobinImpl();
        // 不带并发的轮询

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
