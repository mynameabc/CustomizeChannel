package com.websokcet;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Data
@Slf4j
@Component
public class WebSocketServer {

    public WebSocketServer() {

    }

    public void connect(String userName, Session session) {

    }

    public void close(String userName, Session session) {

    }

    public void error(String userName, Session session, Throwable throwable) {
        this.close(userName, session);
    }

    public void onMessage(String userName, String message) {

    }

    private void sendMessage(String toClientUserName, String message) {

    }
}
