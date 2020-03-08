package com.tangmj.distributed.commons.lockimpl;

import com.tangmj.distributed.commons.LockService;
import com.tangmj.distributed.commons.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tangmingjian 2019-12-18 下午4:16
 **/
@Slf4j
public class RedisLockService implements LockService {
    private static final String LOCK_SCRIPT = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then redis.call('pexpire', KEYS[1], ARGV[2]) return 1 else return 0 end";
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private static final String LOCK_SCRIPT_SHA1 = "c28be0b3-c357-441e-a18e-e306460b2207";
    private static final String UNLOCK_SCRIPT_SHA1 = "f105f09f-6976-4f0f-ba12-e214aaa259f1";


    private static final Long SUCCESS = 1L;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean addLock(String key, String value, long ttl, boolean relet) {
        //秒转毫秒
        final long millisecond = ttl * 1000;
        Long locked = (Long) redisTemplate.execute(new RedisScript<Long>() {
            @Override
            public String getSha1() {
                return LOCK_SCRIPT_SHA1;
            }

            @Nullable
            @Override
            public Class<Long> getResultType() {
                return Long.class;
            }

            @Override
            public String getScriptAsString() {
                return LOCK_SCRIPT;
            }
        }, Collections.singletonList(key), value, millisecond);
        final boolean succeed = SUCCESS.equals(locked);
        if (succeed && relet) {
            ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("relet_".concat(key), true));
            threadPool.scheduleAtFixedRate(() -> redisTemplate.expire(key, millisecond, TimeUnit.SECONDS)
                    , 0, millisecond / 3, TimeUnit.MILLISECONDS);
        }
        return succeed;
    }

    @Override
    public boolean unLock(String key, String value) {
        final Long unLocked = (Long) redisTemplate.execute(new RedisScript<Long>() {
            @Override
            public String getSha1() {
                return UNLOCK_SCRIPT_SHA1;
            }

            @Nullable
            @Override
            public Class<Long> getResultType() {
                return Long.class;
            }

            @Override
            public String getScriptAsString() {
                return UNLOCK_SCRIPT;
            }
        }, Collections.singletonList(key), value);
        return SUCCESS.equals(unLocked);
    }
}
