package com.tangmj.distributed.commons.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

/**
 * @author tangmingjian 2020-04-14 下午10:21
 **/
public class DelayQueueListenerBeanProcessor implements BeanPostProcessor {

    private final DelayQueueListenerRegistry delayQueueListenerRegistry;

    @Autowired
    public DelayQueueListenerBeanProcessor(DelayQueueListenerRegistry delayQueueListenerRegistry) {
        this.delayQueueListenerRegistry = delayQueueListenerRegistry;
    }

    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DelayQueueListener) {
            this.delayQueueListenerRegistry.registerListener((DelayQueueListener) bean);
        }
        return bean;
    }
}
