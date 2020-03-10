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
    com.tangmj.distributed.commons.distributed.lock.type=redis  
    #com.tangmj.distributed.commons.distributed.lock.type=zookeeper  
    
    ###zookeeper方式为可重入锁
   ```
    
   3.使用  
    在需要加锁的方法上加DistributedLock注解，el表达式定义key
    
   ```java
    @Override
        @DistributedLock(key = "#user.userId", prefix = "ACCOUNT:DISTRIBUTEDLOCK:USER")
        public Object handle(User user) {
           
        }
   ```
