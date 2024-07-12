package com.couponcore.model;

import com.couponcore.exception.CouponIssueException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATE;
import static com.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;//쿠폰명

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType; //쿠폰 타입

    private Integer totalQuantity; //쿠폰 발급 최대 수량

    @Column(nullable = false)
    private int issuedQuantity; //발급된 쿠폰 수량

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount; //최소 수량

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;//발급 시작 일시

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd; //발급 종료 일시

    public boolean availableIssueQuantity(){
        if(totalQuantity == null){
            return  true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean availableIssueDate(){
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public boolean isIssueComplete(){
        LocalDateTime now = LocalDateTime.now();
        return dateIssueEnd.isBefore(now) || !availableIssueQuantity();
    }

    public void issue(){
        if(!availableIssueQuantity()){
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. total : %s, issued: %s".formatted(totalQuantity, issuedQuantity));
        }
        if (!availableIssueDate()){
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. request : %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
        issuedQuantity++;
    }

}
