package com.cy.ns.raft;

import com.cy.ns.raft.netty.channel.RaftChannel;
import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Haechi
 * @date 2025/3/16
 */
@SpringBootTest
public class RaftTest {
    @Resource
    RaftChannel raftChannel;



    public static void main( String[] args ) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final long[] start = { System.currentTimeMillis() };
        // 随机选取一个时间作为选举超时时间
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // 随机生成5-10秒的间隔
                int randomInterval = new Random().nextInt(10001);
                Thread.sleep(randomInterval);
                // 执行任务内容
                System.out.println("Start vote.");

                System.out.println("Expression value is : " + (System.currentTimeMillis() - start[0]));
                start[0] = System.currentTimeMillis();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 5, 1, TimeUnit.SECONDS);
    }
}
