package com.tangmj.distributed.commons;

import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-03-30 上午10:02
 * 给单体应用使用，方便单体应用和分布式应用无感切换
 **/
public class LocalDistributedLockService implements DistributedLockService<LocalLock> {

    private ConcurrentHashMap<String, MLock<LocalLock>> locks = new ConcurrentHashMap<>();

    @Override
    public MLock<LocalLock> getLock(String lockName) {
        Assert.notNull(lockName, "'lockName' must not be null");
        return locks.computeIfAbsent(lockName, l -> new MLock(new LocalLock(l)));
    }

    @Override
    public boolean tryLock(MLock<LocalLock> mLock) throws Exception {
        final LocalLock lock = mLock.getLock();
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(MLock<LocalLock> mLock, long waitTime) throws Exception {
        final LocalLock lock = mLock.getLock();
        return lock.tryLock(waitTime, TimeUnit.SECONDS);
    }

    @Override
    public boolean unLock(MLock<LocalLock> mLock) throws Exception {
        final LocalLock lock = mLock.getLock();
        lock.unlock();
        if (lock.getHoldCount() == 0) {
            locks.remove(lock.getLockName());
        }
        return true;
    }
}
