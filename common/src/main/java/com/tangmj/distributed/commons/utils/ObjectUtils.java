package com.tangmj.distributed.commons.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;

/**
 * @author tangmingjian 2019-05-13 下午3:41
 **/
public class ObjectUtils {
    public static <T> T convert(Object source, Class<T> targetClazz) {
        if (source == null) {
            return null;
        }
        T t;
        try {
            t = targetClazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BeanUtils.copyProperties(source, t);
        return t;
    }

    public static <T> void copyProperties(T source, T target, boolean ignoreNullValues) {
        try {
            final Class<?> clazz = source.getClass();
            final Field[] allFields = clazz.getDeclaredFields();
            for (Field field : allFields) {
                field.setAccessible(true);
                final Object value = field.get(source);
                if (value == null && ignoreNullValues) {
                    continue;
                }
                field.set(target, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}


