package com.couponapi.controller;

import com.couponapi.controller.dto.CouponIssueRequestDto;
import com.couponapi.controller.dto.CouponIssueResponseDto;
import com.couponapi.controller.dto.CouponRequestDto;
import com.couponapi.service.CouponIssueRequestService;
import com.couponcore.dto.CouponResponseDto;
import com.couponcore.model.Coupon;
import com.couponcore.service.CouponIssueService;
import com.couponcore.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;


@RequiredArgsConstructor
@RestController
public class CouponIssueController {

    private final CouponService couponService;
    private final CouponIssueRequestService couponIssueRequestService;

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db-info")
    public String getDbInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbProductName = metaData.getDatabaseProductName();
            String dbProductVersion = metaData.getDatabaseProductVersion();
            return "Connected to: " + dbProductName + " - " + dbProductVersion;
        } catch (SQLException e) {
            return "Failed to get database info: " + e.getMessage();
        }
    }
    //쿠폰등록
    @PostMapping("/coupon")
    public CouponResponseDto createCoupon(@RequestBody CouponRequestDto couponRequestDto){
        return couponService.saveCoupon(couponRequestDto.convertToServiceDto());
    }

    @PostMapping("/issue")
    public CouponIssueResponseDto issue (@RequestBody CouponIssueRequestDto requestDto){
        couponIssueRequestService.issueRequest(requestDto);
        return new CouponIssueResponseDto(true, null);

    }


}
