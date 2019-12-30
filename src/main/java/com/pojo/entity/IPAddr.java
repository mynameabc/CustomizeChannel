package com.pojo.entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;

@Data
@Table(name = "ip_addr")
public class IPAddr implements java.io.Serializable {

    private static final long serialVersionUID = -7884092935032142351L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ip_addr_id")
    private Long ipAddrId;

    @Column(name = "url")
    private String url;

    @Column(name = "create_time")
    private Date createTime;
}
