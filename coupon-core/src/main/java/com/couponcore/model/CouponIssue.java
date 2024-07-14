package com.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
//@CreatedDate 쓰기 위해서는 application 실행 부분에 @EnableJpaAuditing 어노테이션으로 Auditing 기능 활성화
//엔티티에는 @EntityListeners(AuditingEntityListener.class) 활성화 해야 작동된다.
@Table(name = "coupon_issues")
public class CouponIssue extends BaseTimeEntity   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long userId;


    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime dateIssued;

    private LocalDateTime dateUsed;

}
