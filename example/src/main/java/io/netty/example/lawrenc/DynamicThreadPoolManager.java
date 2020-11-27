package io.netty.example.lawrenc;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * date  2020/11/26 20:28
 */
public class DynamicThreadPoolManager {

    private final Map<String, DynamicThreadPool> cachePool = new ConcurrentHashMap<>();

    private static final String DEFAULT_POOL = "DEFAULT_POOL";

    public DynamicThreadPoolManager() {
        this(true);
    }

    public DynamicThreadPoolManager(boolean enableDefaultPool) {
        if (enableDefaultPool) {
            DynamicThreadPool defaultPool = new DynamicThreadPool(Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors() << 1,
                    3, TimeUnit.MINUTES
                    , new PoolBlockingQueue<>(1024));
            cachePool.put(DEFAULT_POOL, defaultPool);
        }
    }

    public DynamicThreadPoolManager addPool(String name, DynamicThreadPool pool) {
        if (cachePool.containsKey(name)) {
            throw new InvalidParameterException("this thread pool named " + name + " already exists");
        }

        cachePool.put(name, pool);
        return this;
    }

    public DynamicThreadPool get(String name) {
        return cachePool.get(name);
    }

    public DynamicThreadPool get() {
        return cachePool.get(DEFAULT_POOL);
    }

    public void updateConf(String name, int core, int max, int capacity) {
        DynamicThreadPool threadPool = cachePool.get(name);


        threadPool.setCorePoolSize(core);
        threadPool.setMaximumPoolSize(max);

        if (DynamicThreadPool.isResizeable(threadPool.getQueue())) {
            PoolBlockingQueue<Runnable> queue = (PoolBlockingQueue<Runnable>) threadPool.getQueue();
            queue.setCapacity(capacity);
        }

    }
}