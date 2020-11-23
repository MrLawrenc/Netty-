package io.netty.example.lawrenc.netty;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 疑问
 * <pre>
 *      存储数据结构是数组，但是实际是环？
 *      如果整个数组都冲突则可能会导致死循环吗?
 *      为什么第一次找空槽是逆时针查找？清理的时候顺时针查找？冲突的时候顺时针查找？
 *      启发式清理为什么会发现了容量会左移（扩大两倍）？
 *      如何才会出现内存泄漏？如果没有弱引用存在又会是什么情况？
 * </pre>
 * https://xdkxlk.github.io/2018/11/19/ThreadLocal/ 最先看
 * https://www.cnblogs.com/moderate-fish/p/7658926.html
 * https://www.cnblogs.com/hongdada/p/12108611.html
 * @author : MrLawrenc
 * date  2020/8/9 14:50
 */
@Slf4j
public class ThreadLocalTest {
    ThreadLocal<Integer> local1 = ThreadLocal.withInitial(() -> 1024);
    ThreadLocal<Integer> local2 = new ThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 28;
        }
    };

    volatile boolean stop = false;

    @Test
    public void threadLocal() throws Exception {

        log.info("########################################");
        log.info("local1 init:{}", local1.get());
        log.info("local2 init:{}", local2.get());
        Thread thread = new Thread(() -> {
            int count = 0;
            while (true) {

                LockSupport.park();
                log.info("local1 value({}):{}", ++count, local1.get());
                log.info("local2 value({}):{}", count, local2.get());
                if (stop) {
                    return;
                }
            }
        });
        thread.start();

        LockSupport.unpark(thread);

        TimeUnit.SECONDS.sleep(2);

        log.info("change local value");
        local1.set(9527);
        local2.set(2145);
        stop = true;
        LockSupport.unpark(thread);

        thread.join();
        log.info("########################################");
        local1.remove();
        local2.remove();
    }
}