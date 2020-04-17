package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.core.DelayQueueListenerBeanProcessor;
import com.tangmj.distributed.commons.core.DelayQueueListenerRegistry;
import com.tangmj.distributed.commons.core.DelayQueueTemplate;
import com.tangmj.distributed.commons.redisson.RedissonConfig;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tangmingjian 2020-04-14 下午6:05
 **/
@Configuration
public class DelayQueueAutoConfig extends RedissonConfig {


    @Bean
    public DelayQueueListenerRegistry listenerRegistry(RedissonClient redissonClient) {
        return new DelayQueueListenerRegistry(redissonClient);
    }


    @Bean
    public DelayQueueListenerBeanProcessor listenerBeanProcessor(DelayQueueListenerRegistry listenerRegistry) {
        return new DelayQueueListenerBeanProcessor(listenerRegistry);
    }

    @Bean
    public DelayQueueTemplate delayQueueTemplate(RedissonClient redissonClient) {
        return new DelayQueueTemplate(redissonClient);
    }
}
