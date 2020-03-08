package com.tangmj.distributed.commons.lockimpl;

import com.tangmj.distributed.commons.LockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2019-12-18 下午4:16
 **/
@Slf4j
public class ZkLockService implements LockService {
    private final static String ROOT_PATH = "/locks/";
    @Autowired
    private CuratorFramework zkClient;
    private final static ThreadLocal<InterProcessMutex> MUTEX_THREAD_LOCAL = new ThreadLocal<>();


    @Override
    public boolean addLock(String key, String value, long ttl, boolean relet) {
        InterProcessMutex mutex = new InterProcessMutex(zkClient, ROOT_PATH + key);
        boolean locked = false;
        try {
            locked = mutex.acquire(0L, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("尝试获取锁失败", e);
            return locked;
        }
        MUTEX_THREAD_LOCAL.set(mutex);
        return locked;
    }

    @Override
    public boolean unLock(String key, String value) {
        final InterProcessMutex mutex = MUTEX_THREAD_LOCAL.get();
        if (mutex == null) {
            return true;
        }
        try {
            mutex.release();
            MUTEX_THREAD_LOCAL.remove();
        } catch (Exception e) {
            log.error("释放锁失败", e);
            return false;
        }
        return true;
    }
}
