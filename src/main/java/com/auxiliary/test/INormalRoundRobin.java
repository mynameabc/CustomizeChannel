package com.auxiliary.test;

import com.auxiliary.Server;

public interface INormalRoundRobin {

    /**
     * 初始化
     */
    void init();

    /**
     * 轮询
     * @return
     */
    Object round();
}
