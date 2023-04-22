//package com.game.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
///**
// * @author zcp
// * @date 2021/3/22 20:44
// * 诵我真名者,永不见bug
// */
//@Service
//@Slf4j
//public class RedisService {
//    @Autowired
//    private RedisTemplate<String, Long> redisTemplate;
//
//    public long incr(String key) {
//        try {
//            return redisTemplate.opsForValue().increment(key, 1);
//        } catch (Exception e) {
//            log.error("获取key error->[{}]", e.getMessage());
//        }
//        return -1;
//    }
//}
