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
        issueRequest(couponId, userId,coupon.totalQuantity());
    }

    private void issueRequest(long couponId, long userId,Integer totalIssueQuantity) {
        if(totalIssueQuantity==null){
            redisRepository.issueRequest(couponId,userId,Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId,userId,totalIssueQuantity);
    }


}
