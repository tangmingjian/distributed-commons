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
    * 1. key在redis中不存在，说明锁还没有被任何机器上的任何线程获取，直接存入redis的hash结构中，
    *    field=ARGV[2](即某机器某线程标识) value=1,设置过期时间，返回nil
    * 2. key在redis中存在，且hash中ARGV[2]=1，说明是之前获得到锁得线程再次申请锁(重入)，将value加1，设置过期时间，返回nil
    * 3. 否则说明是其它未获取到锁的线程来申请加锁，返回锁的过期时间
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
    * 1. hash中ARGV[3]的值等于0，说明锁不存在或者不是当前线程加的锁，不能释放锁，直接返回nil
    * 2. hash中ARGV[3]的值减1，结果大于0,说明当前线程之前多次加锁(重入了)，重新设置过期时间，返回0
    *    结果小于等于0直接删除key，publish一个消息？？？ ，返回1
    * 3. 其它返回nil
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

### zookeeper是怎么实现可重入的

```java

    private final ConcurrentMap<Thread, LockData> threadData = Maps.newConcurrentMap();
    
    private static class LockData
    {
        final Thread owningThread;
        final String lockPath;
        final AtomicInteger lockCount = new AtomicInteger(1);
    
        private LockData(Thread owningThread, String lockPath)
        {
            this.owningThread = owningThread;
            this.lockPath = lockPath;
        }
    }
    
    //加锁
    private boolean internalLock(long time, TimeUnit unit) throws Exception
    {
        /*
           Note on concurrency: a given lockData instance
           can be only acted on by a single thread so locking isn't necessary
        */

        Thread currentThread = Thread.currentThread();
        
        LockData lockData = threadData.get(currentThread);
        if ( lockData != null )
        {
            //threadData中能取到LockData，说明是线程再次申请锁，将LockData里的lockCount加1
            // re-entering
            lockData.lockCount.incrementAndGet();
            return true;
        }
        //去zk申请锁
        String lockPath = internals.attemptLock(time, unit, getLockNodeBytes());
        if ( lockPath != null )
        {
            //申请成功后在锁信息放入threadData
            LockData newLockData = new LockData(currentThread, lockPath);
            threadData.put(currentThread, newLockData);
            return true;
        }

        return false;
    }

    //解锁
    public void release() throws Exception
        {
            /*
                Note on concurrency: a given lockData instance
                can be only acted on by a single thread so locking isn't necessary
             */
    
            Thread currentThread = Thread.currentThread();
            LockData lockData = threadData.get(currentThread);
            if ( lockData == null )//当前线程未获取到锁
            {
                throw new IllegalMonitorStateException("You do not own the lock: " + basePath);
            }
    
            int newLockCount = lockData.lockCount.decrementAndGet();
            if ( newLockCount > 0 )
            {
                return;//说明是重入锁，需要多次释放
            }
            if ( newLockCount < 0 )
            {
                throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + basePath);
            }
            try
            {
                internals.releaseLock(lockData.lockPath);//去zk释放锁
            }
            finally
            {
                threadData.remove(currentThread);//将threadData中的LockData清除
            }
```
### zookeeper是怎么实现续期的

zookeeper创建的是顺序临时节点，天然续期，连接断开，锁自动释放


