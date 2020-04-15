package com.tangmj.distributed.commons.core;

/**
 * @author tangmingjian 2020-04-14 下午6:12
 **/
public interface DelayQueueListener<M> {

    void onMessage(M msg);

    String queueName();
}
