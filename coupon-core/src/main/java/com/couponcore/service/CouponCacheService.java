package com.couponcore.service;

import com.couponcore.model.Coupon;
import com.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponIssueService couponIssueService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId){
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }
    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId){
        return getCouponCache(couponId);
    }

    //내부에서 aop를 사용하기 때문에, proxy()사용
    //configuration에 설정 @EnableAspectJAutoProxy(exposeProxy = true)

    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManger")
    public CouponRedisEntity getCouponLocalCache(long couponId){
        return proxy().getCouponCache(couponId);
    }
    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManger")
    public CouponRedisEntity putCouponLocalCache(long couponId){
        return getCouponLocalCache(couponId);
    }


    private CouponCacheService proxy(){return ((CouponCacheService)AopContext.currentProxy());}
}
