package com.tangmj.distributed.commons.core;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tangmingjian 2020-04-14 下午9:38
 **/
@Slf4j
public class DelayQueueListenerRegistry implements ApplicationContextAware, SmartInitializingSingleton {
    private ApplicationContext applicationContext;
    private final Map<String, DelayQueueListener> delayQueueListeners = new HashMap<>();
    private AtomicLong counter = new AtomicLong(0);
    private final RedissonClient redissonClient;

    @Autowired
    public DelayQueueListenerRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void registerListener(DelayQueueListener listener) {
        if (delayQueueListeners.putIfAbsent(listener.queueName(), listener) != null) {
            throw new RuntimeException(String.format("Delay queue name:{} already registered", listener.queueName()));
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        delayQueueListeners.forEach(this::registerContainer);
    }

    private void registerContainer(String beanName, DelayQueueListener delayQueueListener) {
        String containerBeanName = String.format("%s_%s", DelayQueueListenerContainer.class.getName(),
                counter.incrementAndGet());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        genericApplicationContext.registerBean(containerBeanName, DelayQueueListenerContainer.class,
                () -> createListenerContainer(containerBeanName, delayQueueListener));
        DelayQueueListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                DelayQueueListenerContainer.class);
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                log.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }
    }

    private DelayQueueListenerContainer createListenerContainer(String containerBeanName, DelayQueueListener delayQueueListener) {
        DelayQueueListenerContainer delayQueueListenerContainer = new DelayQueueListenerContainer();
        delayQueueListenerContainer.setDistinationQueue(redissonClient.getBlockingDeque(delayQueueListener.queueName(), new JsonJacksonCodec()));
        delayQueueListenerContainer.setListener(delayQueueListener);
        return delayQueueListenerContainer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
