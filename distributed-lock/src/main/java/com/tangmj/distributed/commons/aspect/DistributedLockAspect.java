package com.tangmj.distributed.commons.aspect;

import com.tangmj.distributed.commons.LockService;
import com.tangmj.distributed.commons.annotition.DistributedLock;
import com.tangmj.distributed.commons.exceptions.LockException;
import com.tangmj.distributed.commons.exceptions.UnLockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author tangmingjian 2019-12-18 下午4:09
 **/
@Aspect
@Slf4j
@Order(Integer.MIN_VALUE)
public class DistributedLockAspect {

    @Autowired
    private LockService lockService;

    @Pointcut("@annotation(com.tangmj.distributed.commons.annotition.DistributedLock)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object addLock(ProceedingJoinPoint pjp) throws Throwable {
        final MethodSignature signature = (MethodSignature) pjp.getSignature();
        final Method method = signature.getMethod();
        final DistributedLock lockAnnotation = method.getDeclaredAnnotation(DistributedLock.class);
        final String keyExpressionLanguage = lockAnnotation.key();
        final String prefix = lockAnnotation.prefix();
        ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression expression = expressionParser.parseExpression(keyExpressionLanguage);
        EvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        final Object value = expression.getValue(context);
        String lockKey = prefix.concat(String.valueOf(value));
        if (lockKey == null) {
            log.error("加锁key值为空");
            throw new LockException("加锁key值为空");
        }
        String uuid = UUID.randomUUID().toString();
        final boolean locked = lockService.addLock(lockKey, uuid, lockAnnotation.expirationSeconds(), lockAnnotation.relet());
        log.info("加锁key:[{}]结果:[{}]", lockKey, locked ? "成功" : "失败");
        if (!locked) {
            log.error("加锁key:[{}]失败", lockKey);
            throw new LockException("加锁失败key:".concat(lockKey));
        }
        Object result;
        try {
            //执行业务逻辑
            result = pjp.proceed();
        } finally {
            //释放锁
            final boolean unLock = lockService.unLock(lockKey, uuid);
            log.info("解锁key:[{}]结果:[{}]", lockKey, unLock ? "成功" : "失败");
            if (!unLock) {
                log.error("解锁key:[{}]失败", lockKey);
                throw new UnLockException("解锁失败key:".concat(lockKey));
            }
        }
        return result;
    }
}
