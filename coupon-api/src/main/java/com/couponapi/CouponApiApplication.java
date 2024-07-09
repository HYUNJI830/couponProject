package com.couponapi;

import com.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
//@SpringBootApplication(scanBasePackages = "com")
public class CouponApiApplication {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application-domain,application-api");
        SpringApplication.run(CouponApiApplication.class, args);
    }
}
