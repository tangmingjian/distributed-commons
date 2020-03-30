package com.tangmj.distributed.commons;

import lombok.Data;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tangmingjian 2020-03-30 上午10:31
 **/
@Data
public class LocalLock extends ReentrantLock {
    private String lockName;

    public LocalLock(String lockName) {
        super();
        this.lockName = lockName;
    }
}
