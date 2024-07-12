package com.couponcore.service;

import com.couponcore.dto.CouponResponseDto;
import com.couponcore.dto.CouponServiceDto;
import com.couponcore.model.Coupon;
import com.couponcore.repository.mysql.CouponJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponJpaRepository couponJpaRepository;

    @Transactional
    public CouponResponseDto saveCoupon(CouponServiceDto couponServiceDto){
        Coupon coupon = couponJpaRepository.save(couponServiceDto.toCouponEntity());
        return CouponResponseDto.toEntity(coupon);
    }
}
