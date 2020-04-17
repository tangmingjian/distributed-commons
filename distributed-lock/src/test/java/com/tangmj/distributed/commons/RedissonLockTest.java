package com.tangmj.distributed.commons;

import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tangmingjian 2020-03-09 下午5:11
 **/
public class RedissonLockTest {

    private String redissonNodeAddress = "10.103.22.105:7001,10.103.22.105:7004,10.103.22.106:7002,10.103.22.106:7005,10.103.22.107:7003,10.103.22.107:7006";

    private RedissonClient redissonClient;

    @Before
    public void setup() {
        Config config = new Config();
        final String[] nodes = redissonNodeAddress.split(",");
        final List<String> collect = Arrays.stream(nodes).map(s -> "redis://".concat(s)).collect(Collectors.toList());
        config.useClusterServers()
                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
                //可以用"rediss://"来启用SSL连接
                //.addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
                .addNodeAddress(collect.toArray(new String[]{}));
        redissonClient = Redisson.create(config);
    }

    @Test
    public void testLock() throws Exception {
        String lockKey = "testlock";
        final RLock lock = redissonClient.getLock(lockKey);
        lock.tryLock();
        lock.tryLock();

        lock.unlock();
        lock.unlock();
        Thread.currentThread().join();
    }
}
