package com.couponapi.service;

import com.couponapi.controller.dto.CouponIssueRequestDto;
import com.couponapi.controller.dto.CouponRequestDto;
import com.couponcore.component.DistributeLockExecutor;
import com.couponcore.service.AsyncCouponIssueService;
import com.couponcore.service.AsyncCouponIssueServiceRedis;
import com.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final AsyncCouponIssueService asyncCouponIssueService;
    private final AsyncCouponIssueServiceRedis asyncCouponIssueServiceRedis;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    //1. mydsql 락 구현 (레코드에 락을 걸음)
    public void issueRequest(CouponIssueRequestDto requestDto){
        couponIssueService.issue(requestDto.couponId(),requestDto.userId());
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
    //2. redis 락 구현 (락 이름)
//    public void issueRequest(CouponIssueRequestDto requestDto){
//         distributeLockExecutor.execute("lock_"+requestDto.couponId(),10000,10000,()->{
//             couponIssueService.issue(requestDto.couponId(),requestDto.userId());
//         });
//        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
//    }
    public void asyncIssueRequest(CouponIssueRequestDto requestDto){
        asyncCouponIssueService.issue(requestDto.couponId(),requestDto.userId());
    }

    public void asyncIssueRequestRedis(CouponIssueRequestDto requestDto){
        asyncCouponIssueServiceRedis.issue(requestDto.couponId(),requestDto.userId());
    }



}
