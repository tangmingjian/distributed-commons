package com.tangmj.distributed.commons;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-03-28 下午6:15
 **/
public class RedisDistributedLockService implements DistributedLockService<RLock> {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public MLock<RLock> getLock(String lockName) {
        Assert.notNull(lockName, "'lockName' must not be null");
        final RLock lock = redissonClient.getLock(lockName);
        return new MLock(lock);
    }

    @Override
    public boolean tryLock(MLock<RLock> mLock) throws Exception {
        final RLock lock = mLock.getLock();
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(MLock<RLock> mLock, long waitTime) throws Exception {
        final RLock lock = mLock.getLock();
        return lock.tryLock(waitTime, TimeUnit.SECONDS);
    }

    @Override
    public boolean unLock(MLock<RLock> mLock) throws Exception {
        final RLock lock = mLock.getLock();
        lock.unlock();
        return true;
    }
}
