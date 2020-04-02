package com.java.controller;

import com.java.service.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

/**
 * @description: 控制clientCount为7个线程同时抢锁，谁抢到OK，其他现场则返回null，等到抢到的线程解锁后，计数器减一，i++，其他线程再次抢锁
 * 直到7个线程全部抢到锁，await()通过，程序接着往下执行。
 * @author: ws
 * @time: 2020/3/31 16:26
 */
@RestController
public class RedisLockController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockController.class);
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10,20,1, TimeUnit.MINUTES,new SynchronousQueue<Runnable>());
    @Autowired
    RedisLock redisLock;

    int count = 0;

    @GetMapping("/test")
    public String test() {
        return "hello world";
    }

    @GetMapping("/lockTest")
    public String lockTest() throws InterruptedException {
        int clientCount = 7;
        //倒计时工具(多线程控制工具类)，参数clientCount表示计数数量
        CountDownLatch countDownLatch = new CountDownLatch(clientCount);
        //ExecutorService:线程池接口，newFixedThreadPool():创建固定大小的线程池
//        ExecutorService executorService = Executors.newFixedThreadPool(clientCount);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < clientCount; i++) { //clientcount个线程同时执行 ->{}内的任务
            //额外启一个异步线程
            threadPool.execute(() -> {
                String value = "ws";
                try {
                    redisLock.lock(value);
                    count++;
                    System.out.println("count=" + count);
                } finally {
                    redisLock.unlock(value);
                }
                countDownLatch.countDown(); //计数减一
//                System.out.println("count=" + count);
            });
        }
        countDownLatch.await();//等待，当计数减到0时，所有线程并行执行
        threadPool.shutdown();
        long endTime = System.currentTimeMillis();
        LOGGER.info("执行线程数：{},总耗时：{}ms,count数为：{}",clientCount, endTime-startTime, count);
        return "Hello world";
    }

}
