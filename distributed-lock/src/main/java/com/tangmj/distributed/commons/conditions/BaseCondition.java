package com.tangmj.distributed.commons.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * @author tangmingjian 2019-12-19 下午2:33
 **/
public abstract class BaseCondition implements Condition {
    public static final String LOCK_TYPE_KEY = "com.tangmj.distributed.commons.distributed.lock.type";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String lockType = context.getEnvironment().getProperty(LOCK_TYPE_KEY);
        return !StringUtils.isEmpty(lockType) && lockType.equalsIgnoreCase(lockType());
    }

    protected abstract String lockType();


}
