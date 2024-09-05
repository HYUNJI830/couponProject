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
public class AsyncCouponIssueService {

    private final RedisRepository redisRepository;

    private final CouponCacheService couponCacheService;

    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponIssueRedisService couponIssueRedisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void issue(long couponId, long userId){
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        //쿠폰 캐시를 통한 유효성 검증(쿠폰 존재, 유효 기간)
        coupon.checkIssuableCoupon();
        //분산 락을 통한 동시성 제어
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000,()->{
            //발급 수량 및 중복 발급 검증
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            //비동기 처리로 발급 요청 추가(쿠폰발급을 바로 처리하지 않고, 큐에 적재)
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequest); //String 직렬화
            //쿠폰 발급 요청 추가 (중복 발급 요청 방지)
            //한 유저가 쿠폰을 두번 발급받으려고 시도해도 Set에 이미 저장된 유저라면, 중복 요청 방지함
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            //쿠폰 발급 요청을 Queue 적재(비동기 처리)
            //발급 요청의 대기열 역할
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }


}
