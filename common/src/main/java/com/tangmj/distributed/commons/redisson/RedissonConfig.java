package com.tangmj.distributed.commons.redisson;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tangmingjian 2020-04-16 下午4:04
 **/
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
@Data
public class RedissonConfig {
    @Autowired
    private RedissonProperties redissonProperties;

    @Bean(destroyMethod="shutdown")
    @ConditionalOnProperty(name = "redisson.address")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setDatabase(redissonProperties.getDatabase())
                .setAddress(redissonProperties.getAddress())
                .setTimeout(redissonProperties.getTimeout())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());

        if (StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }


    @Bean(destroyMethod="shutdown")
    @ConditionalOnProperty(name = "redisson.masterName")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonSentinel() {
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers()
                .setDatabase(redissonProperties.getDatabase())
                .addSentinelAddress(redissonProperties.getSentinelAddresses())
                .setMasterName(redissonProperties.getMasterName())
                .setTimeout(redissonProperties.getTimeout())
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());

        if (StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }


    @Bean(destroyMethod="shutdown")
    @ConditionalOnProperty(name = "redisson.nodeAddresses")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonCluster() {
        Config config = new Config();
        final ClusterServersConfig clusterServersConfig = config.useClusterServers()
                .setScanInterval(redissonProperties.getScaninterval())
                .addNodeAddress(redissonProperties.getNodeAddresses())
                .setTimeout(redissonProperties.getTimeout())
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());

        if (StringUtils.isNotBlank(redissonProperties.getPassword())) {
            clusterServersConfig.setPassword(redissonProperties.getPassword());
        }
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
