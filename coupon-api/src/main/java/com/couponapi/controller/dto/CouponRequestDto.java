package com.couponapi.controller.dto;

import com.couponcore.dto.CouponServiceDto;
import com.couponcore.model.Coupon;
import com.couponcore.model.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponRequestDto {

    private Long id;

    private String title;//쿠폰명

    private CouponType couponType; //쿠폰 타입

    private Integer totalQuantity; //쿠폰 발급 최대 수량

    private int issuedQuantity; //발급된 쿠폰 수량

    private int discountAmount;

    private int minAvailableAmount; //최소 수량

    private LocalDateTime dateIssueStart;//발급 시작 일시

    private LocalDateTime dateIssueEnd; //발급 종료 일시

    public CouponServiceDto convertToServiceDto(){
        return CouponServiceDto.builder()
                .id(this.id)
                .title(this.title)
                .couponType(this.couponType)
                .totalQuantity(this.totalQuantity)
                .issuedQuantity(this.issuedQuantity)
                .discountAmount(this.discountAmount)
                .minAvailableAmount(this.minAvailableAmount)
                .dateIssueStart(this.dateIssueStart)
                .dateIssueEnd(this.dateIssueEnd)
                .build();
    }
}
