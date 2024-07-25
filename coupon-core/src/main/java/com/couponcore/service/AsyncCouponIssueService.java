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
    //private final CouponIssueService couponIssueService;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Transactional
//    public void issueEx(long couponId, long userId){
//        //db(엔티티)에서  유효성 검증(쿠폰 존재, 유효 기간)
//        Coupon coupon = couponIssueService.findCoupon(couponId);
//        if(!coupon.availableIssueDate()){
//            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE,"발급 가능한 기간이 아닙니다.");
//        }
//        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000,()-> {
//            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
//                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,
//                        "가능한 수량을 초과 합니다. couponId : %s, userId : %s".formatted(couponId, userId));
//            }
//            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
//                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE,
//                        "이미 발급 요청이 처리되었습니다. couponId : %s, userId : %s".formatted(couponId, userId));
//            }
//        });
//        //쿠폰 발급 요청 추가
//        issueRequest(couponId, userId);
//    }

    @Transactional
    public void issue(long couponId, long userId){
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        //쿠폰 캐시를 통한 유효성 검증(쿠폰 존재, 유효 기간)
        coupon.checkIssuableCoupon();
        //기능 정리(주석 참고)
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000,()->{
            //발급 수량 체크(중복발급, 수량 조회)
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            //쿠폰 발급 요청 추가
            issueRequest(couponId, userId);
        });
    }

    //레디스는 싱글 스레드이기때문에 -> 자동적으로 동시성 이슈가 해결됨
    /*
     1.totalQuantity > redisRepository.sCard(key); //쿠폰 발급 수량 제어
     2.!redisRepository.sIsMember(key, String.valueOf(userId)); //중복 발급 요청 제어
     3.redisRepository.sAdd; : //쿠폰 발급 요청 저장
     4.redisRepository.rPush //쿠폰 발급 큐 적재
     */

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequest); //String 직렬화
            //쿠폰 발급 요청 추가 (키 : 요청의 유니크함과 발급 제어)
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            //쿠폰 발급 Queue 적재 (키 : queue 관리할 키)
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }


}
