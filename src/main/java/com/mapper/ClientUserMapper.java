package com.mapper;

import com.MyMapper;
import com.pojo.entity.ClientUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClientUserMapper extends MyMapper<ClientUser> {

    List<ClientUser> getClientUserForNumber(@Param("number") int number);

    ClientUser getClientUserForName(@Param("name") String name);

    ClientUser getClientUserForWebSocketUserName(@Param("client_name") String client_name);

    ClientUser getClientUserForUsableStatus();
}