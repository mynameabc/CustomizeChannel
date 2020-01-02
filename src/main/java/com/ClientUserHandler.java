package com;

import com.auxiliary.test.NormalRoundRobinWebSocketImpl;
import com.mapper.ClientUserMapper;
import com.pojo.customize.ClientUserInfo;
import com.pojo.entity.ClientUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class ClientUserHandler {

    @Autowired
    private ClientUserMapper clientUserMapper;

    public ClientUser userNamePollSelect() {
        return clientUserMapper.getClientUserForUsableStatus();
    }

    public ClientUser distribute() {

        return null;
    }

    /**
     * webSocket账号再登陆
     * @param webSocketUserName
     * @return
     */
    @Transactional
    public ClientUser againLogin(String webSocketUserName) {

        //改变数据库状态, 解除下单小号与webSocket账号之间的关系
        ClientUser clientUser = clientUserMapper.getClientUserForWebSocketUserName(webSocketUserName);
        if (null != clientUser) {
            clientUser.setClientName("");
            clientUser.setStatus("0");      //0:未分配, 1:已分配
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }

        //选举出新的下单小号
        ClientUser _clientUser = clientUserMapper.getClientUserForUsableStatus();
        if (null != _clientUser) {
            _clientUser.setStatus("1");
            _clientUser.setClientName(webSocketUserName);
            clientUserMapper.updateByPrimaryKeySelective(_clientUser);
        }

        return _clientUser;
    }

    //判断websocket客户端ID是否有绑定的小号, 是则不发登陆信息, 否则发
    @Transactional
    public ClientUserInfo getClientUser(String webSocketUserName) {

        ClientUserInfo clientUserInfo = new ClientUserInfo();

        //判断是否已绑定一个下单小号
        ClientUser clientUser = clientUserMapper.getClientUserForWebSocketUserName(webSocketUserName);
        if (null != clientUser) {
            if (clientUser.getNumber() >= 20) {
                clientUserInfo.setPlaceOrderStatus(0);
            } else {
                clientUserInfo.setPlaceOrderStatus(1);
            }
            clientUserInfo.setStatus(0);
            clientUserInfo.setClientUser(clientUser);
            return clientUserInfo;
        }

        //新取一个下单小号给WebSocket链接
        ClientUser _clientUser = clientUserMapper.getClientUserForUsableStatus();
        if (null != _clientUser) {
            if (_clientUser.getNumber() >= 20) {
                clientUserInfo.setPlaceOrderStatus(0);
            } else {
                clientUserInfo.setPlaceOrderStatus(1);
            }
            clientUserInfo.setStatus(1);
            clientUserInfo.setClientUser(_clientUser);
            return clientUserInfo;
        }

        return null;
    }

    @Transactional
    public void connect(String webSocketUserName, String placeOrderName) {

        //改变数据库状态, 解除下单小号与webSocket账号之间的关系
        ClientUser clientUser = clientUserMapper.getClientUserForName(placeOrderName);
        if (null != clientUser) {
            clientUser.setClientName(webSocketUserName);
            clientUser.setStatus("1");      //0:未分配, 1:已分配
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }
    }

    @Transactional
    public void login(String webSocketUserName) {

        //改变数据库状态, 解除下单小号与webSocket账号之间的关系
        ClientUser clientUser = clientUserMapper.getClientUserForWebSocketUserName(webSocketUserName);
        if (null != clientUser) {
            clientUser.setClientStatus("1");        //客户端状态(0:未登陆, 1:已登陆)
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }
    }

    /**
     * 解除webSocket和小单小号的关系
     * @param webSocketUserName
     */
    @Transactional
    public void removeRelation(String webSocketUserName) {

        //改变数据库状态, 解除下单小号与webSocket账号之间的关系
        ClientUser clientUser = clientUserMapper.getClientUserForWebSocketUserName(webSocketUserName);
        if (null != clientUser) {
            clientUser.setClientStatus("0");    //客户端状态(0:未登陆, 1:已登陆)
            clientUser.setClientName("");
            clientUser.setStatus("0");      //0:未分配, 1:已分配
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }
    }
}
