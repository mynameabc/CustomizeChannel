package com.pojo.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "client_user")
public class ClientUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_user_id")
    private Integer clientUserId;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "number")
    private Integer number;

    @Column(name = "status")
    private String status;

    @Column(name = "place_order_status")
    private String placeOrderStatus;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_status")
    private String clientStatus;

    @Column(name = "proxy_ip")
    private String proxyIp;

    @Column(name = "proxy_port")
    private String proxyPort;

    @Column(name = "proxy_user")
    private String proxyUser;

    @Column(name = "proxy_psw")
    private String proxyPsw;

    @Column(name = "receive_username")
    private String receiveUserName;

    @Column(name = "receive_phone")
    private String receivePhone;

    @Column(name = "receive_address")
    private String receiveAddress;

    @Column(name = "create_time")
    private Date createTime;
}
