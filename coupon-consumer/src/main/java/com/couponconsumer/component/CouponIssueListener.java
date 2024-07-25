package com.couponconsumer.component;

import com.couponcore.repository.redis.RedisRepository;
import com.couponcore.repository.redis.dto.CouponIssueRequest;
import com.couponcore.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.couponcore.utill.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class CouponIssueListener {

    private final RedisRepository redisRepository;

    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper() ;
    private final String issueRequestQueueKey = getIssueRequestQueueKey();

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("Listen...");
        while(existCouponIssueTarget()){
            CouponIssueRequest target = getIssueTarget();
            log.info("발급 시작 target : %s".formatted(target));
            couponIssueService.issue(target.couponId(),target.userId());
            log.info("발급 완료 target : %s".formatted(target));
            removeIssueTarget();
        }
    }

    private void removeIssueTarget() {
        redisRepository.lPop(issueRequestQueueKey);
    }

    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueRequestQueueKey,0),CouponIssueRequest.class);
    }

    private boolean existCouponIssueTarget() {
        return redisRepository.lSize(issueRequestQueueKey) >0;
    }
}
