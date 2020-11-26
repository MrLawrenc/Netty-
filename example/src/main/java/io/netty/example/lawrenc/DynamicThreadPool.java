package io.netty.example.lawrenc;

import java.util.concurrent.*;

/**
 * 动态线程池
 *
 * @author : MrLawrenc
 * date  2020/11/26 20:28
 */
public class DynamicThreadPool extends ThreadPoolExecutor {
    public DynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, warpQueue(workQueue));
    }

    public DynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, warpQueue(workQueue), threadFactory);
    }

    public DynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, warpQueue(workQueue), handler);
    }

    public DynamicThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, warpQueue(workQueue), threadFactory, handler);
    }

    public static PoolBlockingQueue<Runnable> warpQueue(BlockingQueue<Runnable> workQueue) {
        return new PoolBlockingQueue<Runnable>(workQueue.size());
    }
}