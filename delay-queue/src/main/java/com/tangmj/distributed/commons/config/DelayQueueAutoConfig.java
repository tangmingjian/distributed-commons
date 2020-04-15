package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.core.DelayQueueListenerBeanProcessor;
import com.tangmj.distributed.commons.core.DelayQueueListenerRegistry;
import com.tangmj.distributed.commons.core.DelayQueueTemplate;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tangmingjian 2020-04-14 下午6:05
 **/
@Configuration
public class DelayQueueAutoConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(@Value("${redisson.node.address}") String redissonNodeAddress) {
        Config config = new Config();
        final String[] nodes = redissonNodeAddress.split(",");
        final List<String> collect = Arrays.stream(nodes).map(s -> "redis://".concat(s)).collect(Collectors.toList());
        config.useClusterServers()
                .setScanInterval(2000)
                .addNodeAddress(collect.toArray(new String[]{}));
        return Redisson.create(config);
    }

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
