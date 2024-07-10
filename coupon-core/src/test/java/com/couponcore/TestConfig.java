package com.couponcore;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core")
@SpringBootTest(classes = CouponCoreConfiguration.class)
public class TestConfig {
}
//@Transactional : 트랜잭션이 시작되고 종료 될때, 자동으로 커밋하거나 롤백 > DB 일관성 유지와 데이터가 자동으로 롤백
//@ActiveProfiles("test") : 테스트 실행시 활성화할 프로파일 지정, 테스트 환경에 맞는 빈 설정을 로드
//@TestPropertySource(properties = "spring.config.name=application-core") : 특정 프로퍼티 설정한 파일 지정(application-core.properties)