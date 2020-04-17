package com.tangmj.distributed.commons.core;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-04-14 下午6:09
 **/
@Slf4j
public class DelayQueueTemplate<M> {

    private final RedissonClient redissonClient;

    @Autowired
    public DelayQueueTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean offer(String queueName, M msg, long delay, TimeUnit timeUnit) {
        try {
            final RBlockingDeque<M> distinationQueue = redissonClient.getBlockingDeque(queueName);
            final RDelayedQueue<M> delayedQueue = redissonClient.getDelayedQueue(distinationQueue);
            delayedQueue.offer(msg, delay, timeUnit);
            delayedQueue.destroy();
        } catch (Exception e) {
            log.error("延迟队列入队失败", e);
            return false;
        }
        return true;
    }
}
