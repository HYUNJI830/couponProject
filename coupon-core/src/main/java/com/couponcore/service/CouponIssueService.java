package com.couponcore.service;

import com.couponcore.dto.CouponResponseDto;
import com.couponcore.dto.CouponServiceDto;
import com.couponcore.exception.CouponIssueException;
import com.couponcore.model.Coupon;
import com.couponcore.model.CouponIssue;

import com.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.couponcore.repository.mysql.CouponIssueRepository;
import com.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static com.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

@RequiredArgsConstructor
@Service
public class CouponIssueService {
    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    public void issue(long couponId, long userId){
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
        //publishCouponEvent(coupon);
    }
    // 트랜잭션 시작 > lock 획득>issue() > lock 반납>1번 요청 >트랜잭션 커밋 : 트랜잭션 커밋 전에 요청이 남으면 2번 요청시 요청이 안된걸로 봐서 예상보다 많은 요청이 발생함
    // > lock 획득 > 트랜잭션 시작 >issue() >1번 요청 >트랜잭션 커밋> lock 반납 : 트랜잭션 내부에 롹을 열면 안된다.

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId){
        return couponJpaRepository.findById(couponId).orElseThrow(()->{
            throw new CouponIssueException(COUPON_NOT_EXIST,"쿠폰이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

//    @Transactional
//    public Coupon findCouponWithLock(long couponId){
//        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(()->{
//            throw new CouponIssueException(COUPON_NOT_EXIST,"쿠폰이 존재하지 않습니다. %s".formatted(couponId));
//        });
//    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId){
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId,userId);
        if(issue != null){
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id: %d, coupon_id: %d".formatted(userId, couponId));
        }
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(couponIssue);
    }


}
