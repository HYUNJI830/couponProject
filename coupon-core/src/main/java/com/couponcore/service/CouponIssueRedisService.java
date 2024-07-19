package com.couponcore.service;

import com.couponcore.exception.CouponIssueException;
import com.couponcore.repository.redis.RedisRepository;
import com.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity coupon, long userId) {
        if (!availableUserIssueQuantity(coupon.id(), userId)) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "중복 발행한 쿠폰 입니다. couponId : %s, userId: %s".formatted(coupon.id(), userId));
        }
        if (!availableTotalIssueQuantity(coupon.totalQuantity(), coupon.id())) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId : %s, userId : %s".formatted(coupon.id(), userId));
        }
    }

    //수량 여유 확인
    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    //중복 발급 요청 여부 확인
    public boolean availableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }
}
