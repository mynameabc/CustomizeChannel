package com.config;

import com.auxiliary.test.NormalRoundRobinWebSocketImpl;
import com.websokcet.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {  
	
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {  
        return new ServerEndpointExporter();
    }

    @Autowired
    public void setNormalRoundRobinWebSocketImpl(NormalRoundRobinWebSocketImpl normalRoundRobinWebSocketImpl) {
        WebSocket.normalRoundRobinWebSocketImpl = normalRoundRobinWebSocketImpl;
    }
}
