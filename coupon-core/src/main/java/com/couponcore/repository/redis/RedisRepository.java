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
    private final RedisScript<String> issueScript = issueRequestScript(); //이슈 스크립트
    private final String issueRequestQueueKey = getIssueRequestQueueKey(); //요청 큐의 키
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
            //스크립트에서 return 값 =code
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
        //SISMEMBER : 중복 발급 요청 제어
        //sAdd : 쿠폰 발급 요청 저장
        //rPush : 쿠폰 발급 큐 적재 (키가 다른 이유는, 발급 요청과 큐를 구분하기 때문에)
        //return 숫자로 한 이유 -> enum으로 관리하기 위해
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
