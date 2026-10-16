package com.zhj.blog.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();

        // Redis 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Redis 设置默认的序列化方式
        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));

        // Redis设置Key的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Redis设置HashKey的序列化方式
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Redis设置value的序列化方式
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        // Redis设置Hash-value的序列化方式
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));

        return redisTemplate;
    }
}