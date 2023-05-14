package com.geccocrawler.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * 公共工具类
 *
 * @author smilex
 * @date 2023/5/14/16:37
 */
@SuppressWarnings("unused")
@Slf4j
public final class CommonUtils {
    private CommonUtils() {
    }

    /**
     * 获取指定环境变量的值
     *
     * @param name         环境变量名称
     * @param convFunction 转换函数
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 值
     */
    public static <T> T getEnv(String name, Function<String, T> convFunction, T defaultValue) {
        final String value = System.getenv(name);

        if (value == null) {
            if (log.isInfoEnabled()) {
                log.info("getEnv: name {}, value {}", name, defaultValue);
            }

            return defaultValue;
        }

        if (log.isInfoEnabled()) {
            log.info("getEnv: name {}, value {}", name, value);
        }

        return convFunction.apply(value);
    }

    /**
     * 获取指定环境变量的值
     *
     * @param name         环境变量名称
     * @param convFunction 转换函数
     * @param <T>          值类型
     * @return 值
     */
    public static <T> T getEnv(String name, Function<String, T> convFunction) {
        final String value = System.getenv(name);

        if (value == null) {
            throw new RuntimeException(String.format("get env %s is null", name));
        }

        if (log.isInfoEnabled()) {
            log.info("getEnv: name {}, value {}", name, value);
        }

        return convFunction.apply(value);
    }
}
