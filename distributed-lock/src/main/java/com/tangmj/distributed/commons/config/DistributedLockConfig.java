package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.DistributedLockService;
import com.tangmj.distributed.commons.LocalDistributedLockService;
import com.tangmj.distributed.commons.RedisDistributedLockService;
import com.tangmj.distributed.commons.ZookeeperDistributedLockService;
import com.tangmj.distributed.commons.aspect.DistributedLockAspect;
import com.tangmj.distributed.commons.conditions.LocalCondition;
import com.tangmj.distributed.commons.conditions.RedisCondition;
import com.tangmj.distributed.commons.conditions.ZkCondition;
import com.tangmj.distributed.commons.redisson.RedissonConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author tangmingjian 2020-03-08 下午6:49
 **/
@Configuration
public class DistributedLockConfig extends RedissonConfig {

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

    @Bean
    @Conditional(ZkCondition.class)
    public ZookeeperDistributedLockService zookeeperDistributedLockService() {
        return new ZookeeperDistributedLockService();
    }


    @Override
    @Conditional(RedisCondition.class)
    public RedissonClient redissonSingle() {
        return super.redissonSingle();
    }

    @Override
    @Conditional(RedisCondition.class)
    public RedissonClient redissonSentinel() {
        return super.redissonSentinel();
    }

    @Override
    @Conditional(RedisCondition.class)
    public RedissonClient redissonCluster() {
        return super.redissonCluster();
    }

    @Bean
    @Conditional(RedisCondition.class)
    public RedisDistributedLockService redisDistributedLockService() {
        return new RedisDistributedLockService();
    }

    @Bean
    @Conditional(LocalCondition.class)
    public LocalDistributedLockService localDistributedLockService() {
        return new LocalDistributedLockService();
    }

    @Bean
    @ConditionalOnBean(value = DistributedLockService.class)
    public DistributedLockAspect distributedLockAspect() {
        return new DistributedLockAspect();
    }

}
