package com.tangmj.distributed.commons.exceptions;

/**
 * @author tangmingjian 2020-02-18 上午11:10
 **/
public class UnLockException extends RuntimeException {
    public UnLockException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
