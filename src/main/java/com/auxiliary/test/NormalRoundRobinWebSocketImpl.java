package com.auxiliary.test;

import com.pojo.customize.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NormalRoundRobinWebSocketImpl {

    private List<Client> clientList;

    private int totalServer;
    private int currentIndex = -1;

    public void setNumber(Map map) {
        clientList = new ArrayList<>(map.values());
        totalServer = clientList.size();
        currentIndex = totalServer - 1;
    }

    /**
     * 初始化
     */
    public void init() {}

    /**
     * 轮询
     * @return
     */
    public Client round() {
        currentIndex = (currentIndex + 1) % totalServer;
        return clientList.get(currentIndex);
    }

}
