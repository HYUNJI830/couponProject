package com.couponcore;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableJpaAuditing
@ComponentScan
@EnableAutoConfiguration
public class CouponCoreConfiguration {
}

//@EnableAspectJAutoProxy : 스프링에서 AOP 활성화되기 위해 사용
    //AspectJ : 자바언어를 위한 AOP 프레임워크, 해당 스타일 지원
    //exposeProxy = true : 현재 프록시 객체를 노출,
        //AOP 프록시 객체를 접근 가능 -> 같은 클래스내의 다른 메서드를 호출할 때 프록시 사용 가능
//@EnableCaching : 스프링에서 캐싱 활성화
//@EnableJpaAuditing : JPA의 감사기능 -> 엔티티가 생성, 수정될 때 자동으로 생성자, 수정자, 수정시간들을 기록(변경이력추적)
//@ComponentScan : 스프링 특정 패키진 내의 @Component, @Service, @Repository, @Controller 붙은 클래스 검색하고 "빈등록"
    //*basePackages 속성 : 특정 패키지를 지정하지 않으면, 현재 클래스가 속한 패키지를 기본으로 스캔
//@EnableAutoConfiguration : 스프링부트 자동기능 활성화 -> 클래스 패스 설정, 클래스 설정으로 Bean 자동으로 구성