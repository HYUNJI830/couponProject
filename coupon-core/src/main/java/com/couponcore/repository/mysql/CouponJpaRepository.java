package com.couponcore.repository.mysql;

import com.couponcore.model.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    //@Query("select  c from Coupon c where c.id =:id")
    //Optional<Coupon> findCouponWithLock(Long id); //널예외 방지 처리
}

//@Lock : JPA 제공하는 잠금 메커니즘 지정
    //LockModeType.PESSIMISTIC_WRITE : 비관적 잠금 사용(동시성 문제 제거)
    //데이터를 조회할때, db 락을 걸어 다른 트랜잭션이 동시에 데이터 수정 불가(데이터 무결성 보장)
