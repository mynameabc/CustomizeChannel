package com.pojo.customize;

import com.pojo.entity.ClientUser;
import lombok.Data;

@Data
public class ClientUserInfo {

    //新旧标识: 0代表旧 1代表新
    private int status;

    private ClientUser clientUser;

    //下单数量
    private int placeOrderStatus;
}
