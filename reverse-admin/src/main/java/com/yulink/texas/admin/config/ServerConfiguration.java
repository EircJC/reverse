package com.yulink.texas.admin.config;

import com.yulink.texas.core.config.CoreConfiguration;
import com.yulink.texas.common.admin.cache.RedisClient;
import com.yulink.texas.common.admin.config.BaseCoreConfiguration;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Import({
    CoreConfiguration.class,
    BaseCoreConfiguration.class
})
@ComponentScan(basePackages = "com.yulink.texas.admin.*")
public class ServerConfiguration {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    public RedisClient redisClient() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return RedisClient.init(redisTemplate);
    }

}
