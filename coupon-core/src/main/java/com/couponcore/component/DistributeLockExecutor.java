package com.couponcore.component;


import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class DistributeLockExecutor {
    private final RedissonClient redissonClient; //Redisson으로 분산 락 관리
    //Redisson 싱글스레드 방식이기 때문에, 다수 스레드가 동시에 수행하는 것을 방지

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public void execute(String lockName,long waitMillSecond,long leaseMilliSecond ,Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLocked = lock.tryLock(waitMillSecond, leaseMilliSecond, TimeUnit.MILLISECONDS); //락 획득
            if (!isLocked) {
                throw new IllegalStateException("[" + lockName + "] lock 획득 실패");
            }

            logic.run();//락 획득 후 주어진 로직 실행(logic에는 발급 수량,중복 요청 여부 검사)
        } catch (InterruptedException e){
            log.error(e.getMessage(), e);
        } finally{
            if(lock.isHeldByCurrentThread()){
                lock.unlock();//발급처리가 완료되면 락 해제
            }
        }
    }
}
