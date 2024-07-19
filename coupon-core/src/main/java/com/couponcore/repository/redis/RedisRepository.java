package com.couponcore.repository.redis;

import com.couponcore.exception.CouponIssueException;
import com.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestKey;
import static com.couponcore.utill.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor//필수필드를 초기화하는 생성자 자동생성
@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate; //redis 상호작용을 위한 springDataRedis템플릿
    private final RedisScript<String> issueScript = issueRequestScript(); //쿠폰 발행 요청 처리 Lua스크립트
    private final String issueRequestQueueKey = getIssueRequestQueueKey(); //쿠폰 발행 요청 큐
    private final ObjectMapper objectMapper = new ObjectMapper(); //객체 Json 직렬화

    /**
     * Sorted Set
     **/
    //Sorted Set에 값과 점수를 추가.-> 정렬에 따라 순서가 달라지는 이슈 발생
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    /**
     * Set
     **/
    //Set에 값을 추가.
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }
    //Set의 크기 반환
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    //특정 값이 Set에 포함되어 있는지 확인
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }
    /**
     * List
     * queue로 만듬
     **/

    //List의 오른쪽 끝에 값을 추가.
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    // List의 특정 인덱스에 있는 값 반환.
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    //List의 왼쪽 끝에서 값을 제거하고 반환
    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    //List의 크기 반환.
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueueKey),
                    String.valueOf(userId),
                    String.valueOf(totalIssueQuantity),
                    objectMapper.writeValueAsString(couponIssueRequest)
            );
            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(couponIssueRequest));
        }
    }

    private RedisScript<String> issueRequestScript() {
        //SISMEMBER로 사용자id가 이미 set에 있는지 확인 -> 있으면 2반환
        //발생 수량이 set의 크기보다 크면, 사용자 id를 set에 추가 / 요청을 list 추가 ->1 반환
        //그렇지 않으면 3 반환
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }
}
