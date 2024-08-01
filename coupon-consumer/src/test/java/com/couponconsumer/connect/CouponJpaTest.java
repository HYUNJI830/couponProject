package com.couponconsumer.connect;

import com.couponconsumer.TestConfig;
import com.couponcore.model.Coupon;
import com.couponcore.model.CouponIssue;
import com.couponcore.model.CouponType;
import com.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.couponcore.repository.mysql.CouponIssueRepository;
import com.couponcore.repository.mysql.CouponJpaRepository;
import com.couponcore.service.CouponIssueService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class CouponJpaTest extends TestConfig {

    @Autowired
    CouponIssueService service;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Test
    @DisplayName("쿠폰 조회")
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
    }


}
