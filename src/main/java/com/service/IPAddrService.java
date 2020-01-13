package com.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pojo.customize.IPInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPAddrService {

    @Autowired
    private IPAddrMapper ipAddrMapper;

    /**
     * 返回所有IP地址
     * @return
     */
    public List<IPAddr> getList() {
        return ipAddrMapper.selectAll();
    }

    /**
     * 轮询选出ip
     * @return
     */
    public String ipPollSelect() {

        String lock = "1";
        synchronized (lock) {

            Map ipMap = null;
            Integer ipPollValue = 0;

            IPInfo ipInfo = new IPInfo();     //从缓存中获取出
            if (null == ipInfo) {
                List<IPAddr>ipAddrList = ipAddrMapper.selectAll();
                if (null == ipAddrList || ipAddrList.size() >= 0) {
                    return "目前没有可供使用的IP";
                }

                ipMap = ipAddrList.stream().collect(Collectors.toMap(IPAddr::getIpAddrId, a -> a,(k1, k2)->k1));
                ipPollValue = ipMap.size();

                ipInfo.setIpMap(ipMap);
                ipInfo.setIpPollValue(ipPollValue);
            }

            ipMap = ipInfo.getIpMap();                  //目前可用IP
            ipPollValue = ipInfo.getIpPollValue();      //当前IP轮询值

            ipPollValue = (ipPollValue == null) ? (1) : (++ipPollValue);

            int ipCount = ipMap.size();    //ip数量
            if (ipPollValue > ipCount) {
                ipPollValue = 1;
            }

        }

        String ip = "";
        return ip;
    }
}
