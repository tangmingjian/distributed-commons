package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.DistributedLockService;
import com.tangmj.distributed.commons.LocalDistributedLockService;
import com.tangmj.distributed.commons.RedisDistributedLockService;
import com.tangmj.distributed.commons.ZookeeperDistributedLockService;
import com.tangmj.distributed.commons.aspect.DistributedLockAspect;
import com.tangmj.distributed.commons.conditions.LocalCondition;
import com.tangmj.distributed.commons.conditions.RedisCondition;
import com.tangmj.distributed.commons.conditions.ZkCondition;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tangmingjian 2020-03-08 下午6:49
 **/
@Configuration
public class DistributedLockConfig {

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


    @Bean
    @Conditional(RedisCondition.class)
    public RedissonClient redissonClient(@Value("${redisson.node.address}") String redissonNodeAddress) {
        Config config = new Config();
        final String[] nodes = redissonNodeAddress.split(",");
        final List<String> collect = Arrays.stream(nodes).map(s -> "redis://".concat(s)).collect(Collectors.toList());
        config.useClusterServers()
                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
                //可以用"rediss://"来启用SSL连接
                //.addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
                .addNodeAddress(collect.toArray(new String[]{}));
        return Redisson.create(config);
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
