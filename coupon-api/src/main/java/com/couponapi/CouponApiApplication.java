package com.couponapi;

import com.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponApiApplication {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application-core,application-api");
        SpringApplication.run(CouponApiApplication.class, args);
    }
}
