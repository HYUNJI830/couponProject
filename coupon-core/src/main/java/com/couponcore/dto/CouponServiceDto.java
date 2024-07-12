package com.couponcore.dto;

import com.couponcore.model.Coupon;
import com.couponcore.model.CouponType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CouponServiceDto {

    private Long id;

    private String title;//쿠폰명

    private CouponType couponType; //쿠폰 타입

    private Integer totalQuantity; //쿠폰 발급 최대 수량

    private int issuedQuantity; //발급된 쿠폰 수량

    private int discountAmount;

    private int minAvailableAmount; //최소 수량

    private LocalDateTime dateIssueStart;//발급 시작 일시

    private LocalDateTime dateIssueEnd; //발급 종료 일시

    //입력받은 내부클래스의 필드 값을 entity에 주입 (DTO-> 엔티티)
    public Coupon toCouponEntity(){
        return Coupon.builder()
                .id(id)
                .title(title)
                .couponType(couponType)
                .totalQuantity(totalQuantity)
                .issuedQuantity(issuedQuantity)
                .discountAmount(discountAmount)
                .minAvailableAmount(minAvailableAmount)
                .dateIssueStart(dateIssueStart)
                .dateIssueEnd(dateIssueEnd)
                .build();
    }
}
