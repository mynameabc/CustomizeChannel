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

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_status")
    private String clientStatus;

    @Column(name = "now_date")
    private Date nowDate;

    @Column(name = "create_time")
    private Date createTime;
}
