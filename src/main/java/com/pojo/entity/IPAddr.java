package com.pojo.entity;

import java.io.Serializable;
import java.util.Date;

public class IPAddr implements Serializable {
    private Integer ipAddrId;

    private String url;

    private Date createTime;

    private static final long serialVersionUID = 1L;

    public Integer getIpAddrId() {
        return ipAddrId;
    }

    public void setIpAddrId(Integer ipAddrId) {
        this.ipAddrId = ipAddrId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}