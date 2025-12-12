package com.yulink.texas.server.common.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2020/2/26
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Slf4j
@Component
public class RedisManager {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取缓存的值
     * @param cacheKey
     * @return
     */
    public String getCacheValue(String cacheKey){
        String cacheValue=(String)stringRedisTemplate.opsForValue().get(cacheKey);
        return cacheValue;
    }

    /**
     * 判断key是否存在
     * @param cacheKey
     * @return
     */
    public Boolean hasKey(String cacheKey){
        return StringUtils.isNotBlank(getCacheValue(cacheKey));
    }

    /**
     * 设置缓存值
     * @param key
     * @param value
     */
    public void setCacheValue(String key,String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存值并设置有效期
     * @param key
     * @param value
     */
    public void setCacheValueForTime(String key,String value,long time){
        stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 设置key的生命周期
     *
     * @param key
     * @param time
     * @param timeUnit
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        stringRedisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 删除key值
     * @param key
     */
    public void delCacheByKey(String key){
        stringRedisTemplate.opsForValue().getOperations().delete(key);
        stringRedisTemplate.opsForHash().delete("");
    }

    public void delKey(String key){
        stringRedisTemplate.delete(key);
    }

    public void deleteKeys(String keys) {
        Set<String> keysList = stringRedisTemplate.keys(keys);
        if (CollectionUtils.isNotEmpty(keysList)){
            keysList.forEach(i -> {
                try{
                    delKey(i);
                }catch (Exception e){
                    log.info("RedisManager delete keys error:{}",e);
                }
            });
        }
    }

    /**
     * 获取token的有效期
     * @param key
     */
    public long getExpireTime(String key){
        long time = stringRedisTemplate.getExpire(key);
        return time;
    }

    /**
     * 指定时间类型---秒
     * @param key
     * @return
     */
    public long getExpireTimeType(String key){
        long time = stringRedisTemplate.getExpire(key,TimeUnit.SECONDS);
        return time;
    }

    /**
     *
     * @param key---分
     * @return
     */
    public long getExpireTimeTypeForMin(String key){
        long time = stringRedisTemplate.getExpire(key,TimeUnit.MINUTES);
        return time;
    }

    /**
     * 设置一个自增的数据
     * @param key
     * @param growthLength
     */
    public void increment(String key,Long growthLength){
        stringRedisTemplate.opsForValue().increment(key, growthLength);
    }

    public int incrAndGet(String key, int expire) {
        int count = 0;
        String value=(String)stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.opsForValue().increment(key);
        if (StringUtils.isEmpty(value)) {
            stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
            count = 1;
        } else {
            count = Integer.parseInt(value) + 1;
        }
        return count;
    }

    public boolean lock(String key, String value, long expiredTime) {
        if (stringRedisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }

        //避免死锁，且只让一个线程拿到锁
        String currentValue = stringRedisTemplate.opsForValue().get(key);
        //如果锁过期了
        if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < (System.currentTimeMillis()-expiredTime)) {
            //获取上一个锁的时间
            String oldValues = stringRedisTemplate.opsForValue().getAndSet(key, value);

            /*
               只会让一个线程拿到锁
               如果旧的value和currentValue相等，只会有一个线程达成条件，因为第二个线程拿到的oldValue已经和currentValue不一样了
             */
            if (!StringUtils.isEmpty(oldValues) && oldValues.equals(currentValue)) {
                return true;
            }
        }
        return false;
    }

    public void unlock(String key, String value) {
        try {
            String currentValue = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            Map<String, String> tags = new ConcurrentHashMap<>();
            tags.put("uri", "redisManager_unlock");
            tags.put("class",this.getClass().getSimpleName());
            tags.put("code", "000001");
            tags.put("exception",e.getClass().getSimpleName());
            log.error("RedisManager unlock error, {}", e);
        }
    }
}
