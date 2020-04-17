package com.tangmj.distributed.commons.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.slf4j.MDC;
import org.springframework.context.SmartLifecycle;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2020-04-14 下午10:27
 **/
@Slf4j
@Setter
public class DelayQueueListenerContainer implements SmartLifecycle {
    private RBlockingDeque<Object> distinationQueue;
    private DelayQueueListener listener;
    private boolean running;
    private ExecutorService threadPool;
    private volatile boolean stop = false;

    @Override
    public void start() {
        threadPool = new ThreadPoolExecutor(1,
                Runtime.getRuntime().availableProcessors(),
                30,
                TimeUnit.MINUTES,
                new SynchronousQueue(),
                new DelayQueueThreadFactory(queueName()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        startListener();
        setRunning(true);
    }

    private void startListener() {
        new Thread(() -> {
            while (!stop) {
                try {
                    final Object message = distinationQueue.take();
                    threadPool.submit(() -> handleMessage(message));
                } catch (Exception e) {
                    log.error("获取延时任务异常", e);
                }
            }
        }).start();
    }

    private void handleMessage(Object message) {
        try {
            MDC.put("UUID", UUID.randomUUID().toString());
            listener.onMessage(message);
            MDC.remove("UUID");
        } catch (Exception e) {
            log.error("消费延时任务异常", e);
        }
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }


    public String queueName() {
        return listener.queueName();
    }

}
