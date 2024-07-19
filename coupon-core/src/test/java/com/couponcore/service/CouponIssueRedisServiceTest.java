package com.couponcore.service;

import com.couponcore.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.stream.IntStream;

import static com.couponcore.utill.CouponRedisUtils.getIssueRequestKey;


@Slf4j
public class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService couponIssueRedisService;

    @Autowired
    RedisTemplate<String, String> template;

    @BeforeEach
    void clear(){
        Collection<String> redisKeys = template.keys("*");
        template.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true 반환")
    void availableTotalIssueQuantity_1(){
        //given
        int totalIssueQuantity = 10;
        long couponId = 1;
        //when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity,couponId);
        //then
        Assertions.assertTrue(result);

    }

    @Test
    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 소진되면 false 반환")
    void availableTotalIssueQuantity_2(){
        //given
        int totalIssueQuantity = 10;
        long couponId = 1;
        IntStream.range(0,totalIssueQuantity).forEach(userId -> {
            template.opsForSet().add(getIssueRequestKey(couponId),String.valueOf(userId));
        });
        Set<String> members = template.opsForSet().members(getIssueRequestKey(couponId));
        System.out.println("Redis Set Key: " + getIssueRequestKey(couponId));
        System.out.println("Redis Set Members: " + members);

        //when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalIssueQuantity,couponId);
        //then
        Assertions.assertFalse(result);
    }
    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재 하지 않으면 true 반환")
    void availableUserIssueQuantity_1(){
        //given
        long couponId = 1;
        long userId = 1;
        //when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId,userId);
        //then
        Assertions.assertTrue(result);
    }
    @Test
    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 false 반환")
    void availableUserIssueQuantity_2(){
        //given
        long couponId = 1;
        long userId = 1;
        template.opsForSet().add(getIssueRequestKey(couponId),String.valueOf(userId));
        //when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId,userId);
        //then
        Assertions.assertFalse(result);
    }


}
