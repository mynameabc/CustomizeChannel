package com.config;

import com.sun.javaws.security.Resource;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;

@Configuration
@ComponentScan
public class RedissonConfig {

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson(@Value("classpath:redisson-config.yaml") Resource configFile) throws IOException {
        Config config = Config.fromYAML(configFile.getInputStream());
        return Redisson.create(config);
    }
/*    @Value("classpath:/conf/redisson.yaml*/
}
