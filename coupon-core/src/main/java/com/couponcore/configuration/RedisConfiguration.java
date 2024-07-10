package com.couponcore.configuration;


//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//
//@Configuration
//public class RedisConfiguration {
//
//    @Value("${spring.data.redis.host}")
//    private String host;
//
//    @Value("${spring.data.redis.host}")
//    private int port;
//
//    @Bean
//    RedissonClient redissonClient(){
//        Config config = new Config();
//        String address = "redis://" + host + ":" + port;
//        config.useSingleServer().setAddress(address);
//        return Redisson.create(config);
//    }
//
//}
