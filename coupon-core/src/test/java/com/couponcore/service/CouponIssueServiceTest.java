package com.couponcore.service;

import com.couponcore.TestConfig;
import com.couponcore.exception.CouponIssueException;
import com.couponcore.exception.ErrorCode;
import com.couponcore.model.Coupon;
import com.couponcore.model.CouponIssue;
import com.couponcore.model.CouponType;
import com.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.couponcore.repository.mysql.CouponIssueRepository;
import com.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.couponcore.exception.ErrorCode.*;

public class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService service;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    CouponJpaRepository  couponJpaRepository;

    @BeforeEach
    void clean() {
        couponJpaRepository.deleteAllInBatch();
        couponIssueJpaRepository.deleteAllInBatch();
    }
    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다.")
    void saveCouponIssue_1(){
        //given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        //when,then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()-> {
            service.saveCouponIssue(couponIssue.getCouponId(),couponIssue.getUserId());
        });
        Assertions.assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않는다면 쿠폰을 발급한다.")
    void saveCouponIssue_2(){
        long couponId = 1L;
        long userId = 1L;
        CouponIssue result = service.saveCouponIssue(couponId,userId);
        Assertions.assertTrue(couponIssueJpaRepository.findById(result.getId()).isPresent());
        //isPresent : boolean 타입, Optional 객체가 값을 갖고 있다면 true
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰을 발급한다.")
    void issue_1(){
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 시스템")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        long couponId = coupon.getId();

        //쿠폰 발행 수량 체크 (issuedQuantity 증가 시킴)
        service.issue(couponId,userId);

        Coupon couponResult= couponJpaRepository.findById(couponId).get();
        Assertions.assertEquals(couponResult.getIssuedQuantity(),1);

        CouponIssue couponIssueResult = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        Assertions.assertNotNull(couponIssueResult);
    }

    @Test
    @DisplayName("발급 수량에 문제가 있다면 예외를 반환한다.")
    void issue_2(){
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 시스템")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        long couponId = coupon.getId();

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()->{
           service.issue(couponId,userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }



    @Test
    @DisplayName("발급 기한에 문제가 있다면 예외를 반환한다.")
    void issue_3(){
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 시스템")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        long couponId = coupon.getId();

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()->{
            service.issue(couponId,userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("중복 발급 검증에 문제가 있다면 예외를 반환한다.")
    void issue_4(){
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 시스템")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        long couponId = coupon.getId();

        //이슈에 이미 등록했음
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);
        //assertThrows(에러 class, 에러가 발생하는 로직)
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()->{
            service.issue(couponId,userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않는다면 예외를 반환한다.")
    void issue_5(){
        long userId = 1;
        long couponId = 1;
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class,()->{
           service.issue(couponId,userId);
        });
        Assertions.assertEquals(exception.getErrorCode(),COUPON_NOT_EXIST);
    }

}
