package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.LockService;
import com.tangmj.distributed.commons.aspect.DistributedLockAspect;
import com.tangmj.distributed.commons.conditions.RedisCondition;
import com.tangmj.distributed.commons.conditions.ZkCondition;
import com.tangmj.distributed.commons.lockimpl.RedisLockService;
import com.tangmj.distributed.commons.lockimpl.ZkLockService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author tangmingjian 2020-03-08 下午6:49
 **/
@Configuration
public class DistributedLockConfig {

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    @Conditional(RedisCondition.class)
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @Conditional(RedisCondition.class)
    public LockService redisLockService() {
        return new RedisLockService();
    }


    @Bean
    @Conditional(ZkCondition.class)
    public LockService zKLockService() {
        return new ZkLockService();
    }

    @Bean
    @ConditionalOnBean(LockService.class)
    public DistributedLockAspect distributedLockAspect() {
        return new DistributedLockAspect();
    }

    @Bean
    @Conditional(ZkCondition.class)
    public CuratorFramework curatorFramework(@Value("${zk.connectString}") String connectString, @Value("${zk.sessionTimeout:5000}") int sessionTimeoutMs) {
        final CuratorFramework cf = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        cf.start();
        return cf;
    }
}
