package com.couponconsumer.listener;


import com.couponconsumer.component.CouponIssueListener;
import com.couponconsumer.TestConfig;
import com.couponcore.repository.redis.RedisRepository;
import com.couponcore.service.AsyncCouponIssueServiceRedis;
import com.couponcore.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Import(CouponIssueListener.class)
class CouponIssueListenerTest extends TestConfig {

    @Autowired
    CouponIssueListener listener;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    CouponIssueService couponIssueService;
    AsyncCouponIssueServiceRedis asyncCouponIssueServiceRedis;

    @BeforeEach
    void clean() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 없다면 발급을 하지 않는다.")
    void issue_1() throws JsonProcessingException {
        listener.issue();
        verify(couponIssueService,never()).issue(anyLong(),anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 있다면 발급 한다.")
    void issue_2() throws JsonProcessingException {
        //given
        long couponId = 1;
        long userId = 1;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId, totalQuantity);
        //when
        listener.issue();
        //issue
        verify(couponIssueService, times(1)).issue(couponId,userId);
    }

    @Test
    @DisplayName("쿠폰 발급 요청 순서에 맞게 처리 한다.")
    void issue_3() throws JsonProcessingException {
        //given
        long couponId = 1;
        long userId1 = 1;
        long userId2 = 2;
        long userId3 = 3;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId1, totalQuantity);
        redisRepository.issueRequest(couponId, userId2, totalQuantity);
        redisRepository.issueRequest(couponId, userId3, totalQuantity);

        //when
        listener.issue();

        //issue
        InOrder inOrder = Mockito.inOrder(couponIssueService);
        inOrder.verify(couponIssueService, times(1)).issue(couponId,userId1);
        inOrder.verify(couponIssueService, times(1)).issue(couponId,userId2);
        inOrder.verify(couponIssueService, times(1)).issue(couponId,userId3);
    }

}