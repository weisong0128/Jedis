package com.java.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author ws
 * @description 描述
 * @time 2019/11/7 20:32
 */

public class Demo01 {
    public static void main(String[] args) {
        String host = "192.168.74.9";
        int port = 6379;
        Jedis jedis = new Jedis(host, port);
        jedis.auth("123456");

        System.out.println(jedis.ping());
        String value = jedis.get("a");
        System.out.println(value);

        System.out.println(jedis.clientList());

        JedisPoolConfig config = new JedisPoolConfig();


    }
}
