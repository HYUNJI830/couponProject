package com.couponcore.service;

import com.couponcore.component.DistributeLockExecutor;
import com.couponcore.exception.CouponIssueException;
import com.couponcore.exception.ErrorCode;
import com.couponcore.model.Coupon;
import com.couponcore.repository.redis.RedisRepository;
import com.couponcore.repository.redis.dto.CouponIssueRequest;
import com.couponcore.repository.redis.dto.CouponRedisEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestKey;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceRedis {

    private final RedisRepository redisRepository;

    private final CouponCacheService couponCacheService;


    @Transactional
    public void issue(long couponId, long userId){
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);
        coupon.checkIssuableCoupon();
        //distributeLockExecutor.execute 락 제거
        //redis 스크립트를 사용
        issueRequest(couponId, userId,coupon.totalQuantity());
    }

    /* 스크립트로 처리 레디스는 싱글 스레드이기때문에 -> 자동적으로 동시성 이슈가 해결됨
     1.totalQuantity > redisRepository.sCard(key); //쿠폰 발급 수량 제어
     2.!redisRepository.sIsMember(key, String.valueOf(userId)); //중복 발급 요청 제어
     3.redisRepository.sAdd; : //쿠폰 발급 요청 저장
     4.redisRepository.rPush //쿠폰 발급 큐 적재
   */
    //쿠폰발급 대기열에 적재
    private void issueRequest(long couponId, long userId,Integer totalIssueQuantity) {
        if(totalIssueQuantity==null){
            redisRepository.issueRequest(couponId,userId,Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId,userId,totalIssueQuantity);
    }


}
