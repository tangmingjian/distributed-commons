package com.tangmj.distributed.commons;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-03-28 下午6:15
 **/
public class ZookeeperDistributedLockService implements DistributedLockService<InterProcessMutex> {
    private final static String ROOT_PATH = "/locks/";
    @Autowired
    private CuratorFramework zkClient;

    @Override
    public MLock<InterProcessMutex> getLock(String lockName) {
        final InterProcessMutex mutex = new InterProcessMutex(zkClient, ROOT_PATH + lockName);
        return new MLock<>(mutex);
    }

    @Override
    public boolean tryLock(MLock<InterProcessMutex> mLock) throws Exception {
        final InterProcessMutex interProcessMutex = mLock.getLock();
        return interProcessMutex.acquire(0L, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(MLock<InterProcessMutex> mLock, long waitTime) throws Exception {
        final InterProcessMutex interProcessMutex = mLock.getLock();
        return interProcessMutex.acquire(waitTime, TimeUnit.SECONDS);
    }

    @Override
    public boolean unLock(MLock<InterProcessMutex> mLock) throws Exception {
        final InterProcessMutex interProcessMutex = mLock.getLock();
        interProcessMutex.release();
        return true;
    }
}
