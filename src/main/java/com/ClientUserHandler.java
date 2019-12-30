package com;

import com.mapper.ClientUserMapper;
import com.pojo.entity.ClientUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClientUserHandler {

    @Autowired
    private ClientUserMapper clientUserMapper;

    public ClientUser userNamePollSelect() {
        return clientUserMapper.getClientUserForUsableStatus();
    }

    /**
     * webSocket账号再登陆
     * @param webSocketUserName
     * @return
     */
    @Transactional
    public ClientUser againLogin(String webSocketUserName) {

        //选举出新的下单小号
        ClientUser _clientUser = clientUserMapper.getClientUserForUsableStatus();
        if (null != _clientUser) {
            _clientUser.setStatus("1");
            _clientUser.setClientName(webSocketUserName);
            clientUserMapper.updateByPrimaryKeySelective(_clientUser);
        }

        //改变数据库状态, 解除下单小号与webSocket账号之间的关系
        ClientUser clientUser = clientUserMapper.getClientUserForWebSocketUserName(webSocketUserName);
        if (null != clientUser) {
            clientUser.setClientName("");
            clientUser.setStatus("0");      //0:未分配, 1:已分配
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }

        return _clientUser;
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
            clientUser.setClientName("");
            clientUser.setStatus("0");      //0:未分配, 1:已分配
            clientUserMapper.updateByPrimaryKeySelective(clientUser);
        }
    }
}
