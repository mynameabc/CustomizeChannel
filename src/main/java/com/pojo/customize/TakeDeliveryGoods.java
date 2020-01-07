package com.pojo.customize;

import lombok.Data;


@Data
public class TakeDeliveryGoods implements java.io.Serializable {

    private String name;

    private String password;

    private String clientName;

    private String proxyIp;

    private String proxyPort;

    private String proxyUser;

    private String proxyPsw;

    private String clientOrderNo;

    private String platformOrderNo;
}
