package com.couponapi.service;

import com.couponapi.controller.dto.CouponIssueRequestDto;
import com.couponapi.controller.dto.CouponRequestDto;
import com.couponcore.component.DistributeLockExecutor;
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

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());



    public void issueRequest(CouponIssueRequestDto requestDto){
         distributeLockExecutor.execute("lock_"+requestDto.couponId(),10000,10000,()->{
             couponIssueService.issue(requestDto.couponId(),requestDto.userId());
         });
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
    //1. mydsql 락 구현
    //2. redis 락 구현

}
