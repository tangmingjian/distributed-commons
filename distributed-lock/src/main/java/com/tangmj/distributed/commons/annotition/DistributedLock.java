package com.tangmj.distributed.commons.annotition;

import java.lang.annotation.*;

/**
 * @author tangmingjian 2020-03-08 下午6:36
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DistributedLock {
    /**
     * 锁前缀
     *
     * @return
     */
    String prefix() default "DISTRIBUTEDLOCK:";

    /**
     * 锁key值／el表达式
     *
     * @return
     */
    String key();

    /**
     * 获锁等待时间
     *
     * @return
     */
    long waitTimeSeconds() default 0L;

}
