package com.yulink.texas.common.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: liupanpan
 * @Date: 2018/11/6
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

@Slf4j
public class ExpiredMapCache<K, V> {

    public static final int CONCURRENCY_LEVEL = 3;


    private ConcurrentMap<K, V> cacheMap;

    public ExpiredMapCache(int maxSize, int duration, TimeUnit timeUnit) {
        Cache<K, V> cache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(duration, timeUnit)
            .concurrencyLevel(CONCURRENCY_LEVEL)
            .removalListener(
                notification -> log.debug("remove expire|key:{}|values:{}|cause:{}",
                    notification.getKey(),
                    notification.getValue(),
                    notification.getCause()
                )
            )
            .recordStats()
            .build();
        cacheMap = cache.asMap();
    }

    public void put(K key, V value) {
        cacheMap.put(key, value);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }

    public boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

    public V getOrDefault(K key,V defaultValue){
        return cacheMap.getOrDefault(key, defaultValue);
    }

}
