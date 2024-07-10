package com.couponapi.controller;

import com.couponapi.controller.dto.CouponIssueRequestDto;
import com.couponapi.controller.dto.CouponIssueResponseDto;
import com.couponapi.service.CouponIssueRequestService;
import com.couponcore.model.Coupon;
import com.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

//    @PostMapping("/test")
//    public void test (){
//        Coupon coupon = Coupon.builder()
//                .totalQuantity(100)
//                .issuedQuantity(99)
//                .dateIssueStart(LocalDateTime.now().minusDays(1))
//                .dateIssueEnd(LocalDateTime.now().plusDays(2))
//                .build();
//    }

    @PostMapping("/issue")
    public CouponIssueResponseDto issue (@RequestBody CouponIssueRequestDto requestDto){
        couponIssueRequestService.issueRequest(requestDto);
        return new CouponIssueResponseDto(true, null);

    }


}
