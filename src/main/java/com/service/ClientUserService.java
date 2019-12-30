package com.service;

import com.pojo.entity.ClientUser;
import com.mapper.ClientUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientUserService {

    @Autowired
    private ClientUserMapper clientUserMapper;

    public List<ClientUser> getList() {
        return clientUserMapper.selectAll();
    }

    /**
     * 轮询选出账号
     * @return
     */
    public String userNamePollSelect() {

        String userName = "news";

        return userName;
    }
}
