package com.tangmj.distributed.commons;

/**
 * @author tangmingjian 2019-12-18 下午4:15
 **/
public interface LockService {

    /**
     * 加锁
     *
     * @param key   key
     * @param value value(作为加锁者的标识)
     * @param ttl   过期时间(秒)
     * @param relet 是否续租
     * @return
     */
    boolean addLock(String key, String value, long ttl, boolean relet);

    /**
     * 解锁
     *
     * @param key   key
     * @param value value(作为解锁者的标识)
     * @return
     */
    boolean unLock(String key, String value);
}
