package com.tangmj.distributed.commons.aspect;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-03-10 上午10:20
 **/
@Slf4j
public class RedisDistributedLockAspect extends DistributedLockAspect<RLock> {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    RLock getLock(String lockName) {
        return redissonClient.getLock(lockName);
    }

    @Override
    boolean tryLock(RLock rLock) throws Exception {
        return rLock.tryLock();
    }

    @Override
    protected boolean tryLock(RLock rLock, long waitTime) throws Exception {
        return rLock.tryLock(waitTime, TimeUnit.SECONDS);
    }

    @Override
    boolean unLock(RLock rLock) throws Exception {
        rLock.unlock();
        return true;
    }
}
