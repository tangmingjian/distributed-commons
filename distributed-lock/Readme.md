## 分布式锁

### 分布式锁要解决的问题
* 加锁／解锁
* 解铃必须系铃人
* 可重入
* 续租

### 分布式锁方案
* redis lua脚本  加锁将setnx和pexpire命令包成一个原子操作，解锁校验value值成功才能解锁。重入和续期需要自己实现
* [redisson](https://github.com/redisson/redisson/wiki) 基于Redis的Redisson分布式可重入锁RLock Java对象实现了java.util.concurrent.locks.Lock接口
* [zookeeper Curator](http://curator.apache.org/getting-started.html) 

### 本项目使用     
   1.引入jar  
   ```xml
        <dependency>
            <groupId>com.tangmj.distributed.commons</groupId>
            <artifactId>distributed-lock</artifactId>
            <version>${version}</version>
        </dependency>
   ```
   2.分布式锁中间件选择(redis/zookeeper)  
   ```properties
    ##选用redis做锁
    com.tangmj.distributed.commons.distributed.lock.type=redis  
    #redisson集群配置  
    redisson.node.address=10.103.22.105:7001,10.103.22.105:7004,10.103.22.106:7002,10.103.22.106:7005,10.103.22.107:7003,10.103.22.107:7006      

    ##选用zookeeper做锁  
    #com.tangmj.distributed.commons.distributed.lock.type=zookeeper   
    #zk地址  
    #zk.connectString=zk.gpayintra.com:2182,zk.gpayintra.com:2183  

    
   ```
    
   3.使用  
    在需要加锁的方法上加DistributedLock注解，el表达式定义key
    
   ```java
    @Override
        @DistributedLock(key = "#user.userId", prefix = "ACCOUNT:DISTRIBUTEDLOCK:USER")
        public Object handle(User user) {
           
        }
   ```

### redisson怎么实现重入
直接上代码
```java
/**
* 加锁
**/
<T> RFuture<T> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        internalLockLeaseTime = unit.toMillis(leaseTime);

        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
                  "if (redis.call('exists', KEYS[1]) == 0) then " +
                      "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                      "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                      "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                      "return nil; " +
                  "end; " +
                  "return redis.call('pttl', KEYS[1]);",
                    Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
    }
    
    protected String getLockName(long threadId) {
        //id即为机器标识，threadId代表线程
        return id + ":" + threadId;
    }
    public RedissonLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;
        this.id = commandExecutor.getConnectionManager().getId();
        this.internalLockLeaseTime = commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout();
        this.entryName = id + ":" + name;
        this.pubSub = commandExecutor.getConnectionManager().getSubscribeService().getLockPubSub();
    }
    /**
    * key在redis中不存在，说明是某个线程第一申请加锁，直接存入hash中，value=1,设置过期时间，返回nil
    * key在hash中值为1，说明某个线程再次来申请加锁，将value加1，设置过期时间，返回nil
    * 否则说明是其它线程来申请加锁，返回锁的过期时间
    **/
    
    /**
    * 解锁
    **/
    protected RFuture<Boolean> unlockInnerAsync(long threadId) {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                    "return nil;" +
                "end; " +
                "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 0; " +
                "else " +
                    "redis.call('del', KEYS[1]); " +
                    "redis.call('publish', KEYS[2], ARGV[1]); " +
                    "return 1; "+
                "end; " +
                "return nil;",
                Arrays.<Object>asList(getName(), getChannelName()), LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId));

    }
    /**
    * key的value值为0，直接放回
    * key的value值减1，结果大于0重新设置过期时间，
    * 小于等于0直接删除key，publish一个消息？？？ 
    **/
   
```

### redisson怎么实现续期
直接上代码
```java
private void renewExpiration() {
        ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName());
        if (ee == null) {
            return;
        }
        
        Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
                if (ent == null) {
                    return;
                }
                Long threadId = ent.getFirstThreadId();
                if (threadId == null) {
                    return;
                }
                
                RFuture<Boolean> future = renewExpirationAsync(threadId);
                future.onComplete((res, e) -> {
                    if (e != null) {
                        log.error("Can't update lock " + getName() + " expiration", e);
                        return;
                    }
                    
                    if (res) {
                        // reschedule itself
                        renewExpiration();
                    }
                });
            }
            //过期时间三分一时触发
        }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);
        
        ee.setTimeout(task);
    }

    //触发一个定时任务
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        try {
            //netty的HashedWheelTimer
            return timer.newTimeout(task, delay, unit);
        } catch (IllegalStateException e) {
            // timer is shutdown
            return dummyTimeout;
        }
    }
    //续期lua脚本
    protected RFuture<Boolean> renewExpirationAsync(long threadId) {
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return 1; " +
                "end; " +
                "return 0;",
            Collections.<Object>singletonList(getName()), 
            internalLockLeaseTime, getLockName(threadId));
    }
```


