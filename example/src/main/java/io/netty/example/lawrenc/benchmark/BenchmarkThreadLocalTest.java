package io.netty.example.lawrenc.benchmark;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

/**
 * @author MrLawrenc
 * date  2020/9/28 22:11
 * <p>
 * parent pom中控制gc
 * <argLine.printGC>-XX:+PrintGCDetails</argLine.printGC>
 */
public class BenchmarkThreadLocalTest {

    /**
     * 测试ThreadLocal运行耗时
     *
     * @param threadLocalCount ThreadLocal的对象数量
     * @param runCount         调用get方法的次数
     * @param value            ThreadLocal的局部变量值
     */
    public static void testThreadLocal(int threadLocalCount, int runCount, String value) {
        final ThreadLocal<String>[] caches = new ThreadLocal[threadLocalCount];
        final Thread mainThread = Thread.currentThread();
        IntStream.range(0, threadLocalCount).forEach(i -> caches[i] = new ThreadLocal());

        Thread t = new Thread(() -> {
            for (int i = 0; i < threadLocalCount; i++) {
                caches[i].set(value);
            }
            long start = System.nanoTime();
            for (int i = 0; i < threadLocalCount; i++) {
                for (int j = 0; j < runCount; j++) {
                    caches[i].get();
                }
            }
            long end = System.nanoTime();
            System.out.println("thread local take[" + TimeUnit.NANOSECONDS.toMillis(end - start) +
                    "]ms");
            LockSupport.unpark(mainThread);
        });
        t.start();
        LockSupport.park(mainThread);
    }


    /**
     * 测试FastThreadLocal运行耗时
     */
    public static void testFastThreadLocal(int threadLocalCount, int runCount, String value, boolean userSlowThread) {
        final FastThreadLocal<String>[] caches = new FastThreadLocal[threadLocalCount];
        final Thread mainThread = Thread.currentThread();

        IntStream.range(0, threadLocalCount).forEach(i -> caches[i] = new FastThreadLocal());

        Runnable runnable = () -> {
            for (int i = 0; i < threadLocalCount; i++) {
                caches[i].set(value);
            }
            long start = System.nanoTime();
            for (int i = 0; i < threadLocalCount; i++) {
                for (int j = 0; j < runCount; j++) {
                    caches[i].get();
                }
            }
            long end = System.nanoTime();
            System.out.println("fast thread local take[" + TimeUnit.NANOSECONDS.toMillis(end - start) + "]ms");
            LockSupport.unpark(mainThread);
        };

        //注意使用FastThreadLocal
        Thread t = userSlowThread ? new Thread(runnable) : new FastThreadLocalThread(runnable);
        t.start();
        LockSupport.park(mainThread);
    }


    /**
     * 对比ThreadLocal和 FastThreadLocal的耗时
     */
    @Test
    public void test() {
        String value = "thread local value";
        //thread local take[312]ms     fast thread local take[15]ms
/*        testThreadLocal(10000, 10000, value);
        testFastThreadLocal(10000, 10000, value,false);*/

        //thread local take[303]ms   fast thread local take[599]ms
        /*testThreadLocal(10000, 10000, value);
        testFastThreadLocal(10000, 10000, value,true);*/

        //thread local take[3247]ms  fast thread local take[15]ms
       /* testThreadLocal(1000, 1000000, value);
        testFastThreadLocal(1000, 1000000, value,false);*/

        //thread local take[3263]ms  fast thread local take[5415]ms
        testThreadLocal(1000, 1000000, value);
        testFastThreadLocal(1000, 1000000, value, true);
    }


}

