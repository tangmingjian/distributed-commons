## 延时队列

### 实现方案  

   * java DelayQueue  
   * 时间轮   
     * netty 
     * kafka   
   * redisson 
   * rocketmq
   * RabbitMQ
   
   ##### 对比
   java DelayQueue、时间轮无法持久化，系统重启消息丢失  
   rocketmq 无法任意指定延时时间  
   RabbitMQ erlang  
   
### 要解决的问题
   * 延时时间任意
   * 消息持久化，不丢失
   * 分布式  
      
### 本模块使用步骤

1. 引入jar  
   ```xml
        <dependency>
            <groupId>com.tangmj.distributed.commons</groupId>
            <artifactId>delay-queue</artifactId>
            <version>${version}</version>
        </dependency>
   ```    
2. 配置redisson

    ```properties
        #redisson单机配置  
        redisson.address=localhost:6379
    
        ##redisson哨兵配置  
        #redisson.masterName=masterName
        #redisson.sentinelAddresses=ip1:port1,ip1:port2,ip1:port3

        ##redisson集群配置  
        #redisson.nodeAddresses=ip1:port1,ip1:port2,ip2:port1,ip2:port2,ip3:port1,ip3:port2
    ```
  
3. 引入DelayQueueTemplate
    ```java
        @Autowired
        private DelayQueueTemplate delayQueueTemplate;
    
        //发送延时消息
        delayQueueTemplate.offer("delay-message-queue-name", delayMessage, i, TimeUnit.SECONDS)
    
    ``` 

4. 实现DelayQueueListener接口
    ```java
        @Component
        @Slf4j
        public class MyDelayQueueListener implements DelayQueueListener<DelayMessage> {
            @Override
            public void onMessage(DelayMessage delayMessage) {
                //todo 处理消息
            }
        
            @Override
            public String queueName() {
                return "delay-message-queue-name";
            }
        }
    
    ```