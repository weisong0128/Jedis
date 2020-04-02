package com.java.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * @description: 单机版redis分布式锁测试 ,这种方式key value不具有可重入性
 * @author: ws
 * @time: 2020/3/31 16:21
 */
@Service
public class RedisLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

    private static String lock_key = "redis_lock"; //锁键
    private static int lock_expire_time = 30;    //锁过期时间，单位：s
    private static int time_out = 1000000;   //获取锁的超时时间，单位：ms
    private static JedisPool jedisPool = null;

    //set命令的参数
    SetParams setParams = SetParams.setParams().nx().ex(lock_expire_time);

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        jedisPool = new JedisPool(config,"192.168.74.9", 6379, 300,"123456");
    }

    /**
     * 加锁
     * @param value
     * @return
     */
    public Boolean lock(String value) {
        Jedis jedis = jedisPool.getResource();  //从连接池中获取一个jedis连接对象
        long startTime = System.currentTimeMillis();
        try {
            for(;;) {
                //set命令返回ok，则证明获取锁成功
                String lock = jedis.set(lock_key, value, setParams);
                LOGGER.info("返回值：{}", lock);
                if("OK".equals(lock)) {
//                    LOGGER.info("返回状态：true");
                    return true;
                }
                //否则循环等待，在timeout时间内仍未获取到锁，则获取失败
                long endTime = System.currentTimeMillis();
                long resultTime = endTime - startTime;
                if (resultTime >= time_out) {
//                    LOGGER.error("返回状态：false");
                    return false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            jedis.close();
        }
    }


    public boolean unlock(String value) {
        Jedis jedis = jedisPool.getResource();
        String script =
                "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                        "   return redis.call('del',KEYS[1]) " +
                        "else" +
                        "   return 0 " +
                        "end";

        try {
            Object result = jedis.eval(script, Collections.singletonList(lock_key), Collections.singletonList(value));
            LOGGER.info("result值为：{}", result);
            if("1".equals(result.toString())) {
                return true;
            }
            return false;
        } finally {
            jedis.close();
        }

    }
}
