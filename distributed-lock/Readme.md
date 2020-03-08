## 分布式锁

### 1.引入jar  
```xml
    <dependency>
        <groupId>com.tangmj.distributed.commons</groupId>
        <artifactId>distributed-lock</artifactId>
        <version>${version}</version>
    </dependency>
```
### 2.分布式锁中间件选择(redis/zookeeper)  
```properties
com.tangmj.distributed.commons.distributed.lock.type=redis  
#com.tangmj.distributed.commons.distributed.lock.type=zookeeper  

###zookeeper方式为可重入锁
```

### 3.使用  
在需要加锁的方法上加DistributedLock注解，el表达式定义key

```java
@Override
    @DistributedLock(key = "#user.userId", prefix = "ACCOUNT:DISTRIBUTEDLOCK:USER")
    public Object handle(User user) {
       
    }
```
