package com.tangmj.distributed.commons.config;

import com.tangmj.distributed.commons.aspect.DistributedLockAspect;
import com.tangmj.distributed.commons.aspect.RedisDistributedLockAspect;
import com.tangmj.distributed.commons.aspect.ZookeeperDistributedLockAspect;
import com.tangmj.distributed.commons.conditions.RedisCondition;
import com.tangmj.distributed.commons.conditions.ZkCondition;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public DistributedLockAspect zkDistributedLockAspect() {
        return new ZookeeperDistributedLockAspect();
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
    public DistributedLockAspect redisDistributedLockAspect() {
        return new RedisDistributedLockAspect();
    }

}
