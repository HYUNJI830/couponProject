package com.couponcore.service;

import com.couponcore.TestConfig;
import com.couponcore.exception.CouponIssueException;
import com.couponcore.exception.ErrorCode;
import com.couponcore.model.Coupon;
import com.couponcore.model.CouponType;
import com.couponcore.repository.mysql.CouponJpaRepository;
import com.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;

import static com.couponcore.utill.CouponRedisUtils.getIssueRequestKey;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

class AsyncCouponIssueServiceRedisTest extends TestConfig {
    @Autowired
    AsyncCouponIssueServiceRedis asyncCouponIssueService;

    @Autowired
    RedisTemplate<String, String> template;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void clear(){
        Collection<String> redisKeys = template.keys("*");
        template.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰이 존재 하지 않는 다면 예외를 반환")
    void issue_1(){
        //given
        long couponId = 1;
        long userId = 1;
        //when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()->{
            asyncCouponIssueService.issue(couponId,userId);
        });
        //then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);

    }

    @Test
    @DisplayName("쿠폰 발급 - 발급 가능 수량이 존재하지 않는다면 예외를 반환한다")
    void issue_2(){
        // given
        long userId = 100;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        IntStream.range(0,coupon.getTotalQuantity()).forEach(idx ->{
            template.opsForSet().add(getIssueRequestKey(coupon.getId()),String.valueOf(idx));
        });
        //when,then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급된 유저라면 예외를 반환한다")
    void issue_3(){
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);
        template.opsForSet().add(getIssueRequestKey(coupon.getId()),String.valueOf(userId));

        //when,then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(),ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 - 발급 기한이 유효하지 않다면 예외를 반환한다")
    void issue_4(){
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);
        template.opsForSet().add(getIssueRequestKey(coupon.getId()),String.valueOf(userId));

        //when,then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueService.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(),ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }
    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급을 기록한다")
    void issue_5(){
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);
        //when,then
        asyncCouponIssueService.issue(coupon.getId(),userId);
        Boolean isSaved = template.opsForSet().isMember(getIssueRequestKey(coupon.getId()),String.valueOf(userId));
        Assertions.assertTrue(isSaved);
    }

    @Test
    @DisplayName("쿠폰 발급 - 쿠폰 발급 요청이 성공하면 쿠폰 발급 큐에 적재된다.")
    void issue_6() throws JsonProcessingException {
        // given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        couponJpaRepository.save(coupon);
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId(),userId);

        //when,then
        asyncCouponIssueService.issue(coupon.getId(),userId);
        String savedIssueRequest = template.opsForList().leftPop(getIssueRequestQueueKey());
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(request),savedIssueRequest);
    }
}