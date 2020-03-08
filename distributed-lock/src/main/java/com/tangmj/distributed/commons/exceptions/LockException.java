package com.tangmj.distributed.commons.exceptions;

/**
 * @author tangmingjian 2020-02-18 上午11:10
 **/
public class LockException extends RuntimeException {
    public LockException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
