package com.service;

import com.mapper.ClientUserMapper;
import com.pojo.entity.ClientUser;
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
}
