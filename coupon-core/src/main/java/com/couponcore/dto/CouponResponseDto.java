package com.couponcore.dto;

import com.couponcore.model.Coupon;
import com.couponcore.model.CouponType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponResponseDto {

    private Long id;

    private String title;//쿠폰명

    private CouponType couponType; //쿠폰 타입

    private Integer totalQuantity; //쿠폰 발급 최대 수량

    private int issuedQuantity; //발급된 쿠폰 수량

    private int discountAmount;

    private int minAvailableAmount; //최소 수량

    private LocalDateTime dateIssueStart;//발급 시작 일시

    private LocalDateTime dateIssueEnd; //발급 종료 일시

    //외부 클래스 필드에 입력 해주는 작업(Entity 값을)
    public static CouponResponseDto toEntity(Coupon coupon){
       CouponResponseDto couponResponseDto =new CouponResponseDto();
       couponResponseDto.id = coupon.getId();
       couponResponseDto.title = coupon.getTitle();
       couponResponseDto.couponType = coupon.getCouponType();
       couponResponseDto.totalQuantity = coupon.getTotalQuantity();
       couponResponseDto.issuedQuantity = coupon.getIssuedQuantity();
       couponResponseDto.discountAmount = coupon.getDiscountAmount();
       couponResponseDto.minAvailableAmount = coupon.getMinAvailableAmount();
       couponResponseDto.dateIssueStart = coupon.getDateIssueStart();
       couponResponseDto.dateIssueEnd = coupon.getDateIssueEnd();
        return couponResponseDto;
    }
}
