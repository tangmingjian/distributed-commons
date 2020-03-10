package com.tangmj.distributed.commons.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-03-10 上午11:12
 **/
@Slf4j
public class ZookeeperDistributedLockAspect extends DistributedLockAspect<InterProcessMutex> {
    private final static String ROOT_PATH = "/locks/";
    @Autowired
    private CuratorFramework zkClient;

    @Override
    InterProcessMutex getLock(String lockName) {
        return new InterProcessMutex(zkClient, ROOT_PATH + lockName);

    }

    @Override
    boolean tryLock(InterProcessMutex interProcessMutex) throws Exception {
        return interProcessMutex.acquire(0L, TimeUnit.SECONDS);
    }

    @Override
    protected boolean tryLock(InterProcessMutex interProcessMutex, long waitTime) throws Exception {
        return interProcessMutex.acquire(waitTime, TimeUnit.SECONDS);
    }

    @Override
    boolean unLock(InterProcessMutex interProcessMutex) throws Exception {
        interProcessMutex.release();
        return true;
    }
}
