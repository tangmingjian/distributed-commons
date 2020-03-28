package com.tangmj.distributed.commons;

/**
 * @author tangmingjian 2020-03-28 下午6:12
 **/
public interface DistributedLockService<Lock> {

    MLock<Lock> getLock(String lockName);

    boolean tryLock(MLock<Lock> mLock) throws Exception;

    boolean tryLock(MLock<Lock> mLock, long waitTime) throws Exception;

    boolean unLock(MLock<Lock> mLock) throws Exception;
}
