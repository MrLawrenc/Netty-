package io.netty.example.lawrenc.netty;

import io.netty.util.concurrent.FastThreadLocal;
import org.junit.Test;

/**
 * @author : MrLawrenc
 * date  2020/8/9 14:45
 * <p>
 * GC信息可以在pom中关闭  <argLine.printGC>-XX:+PrintGCDetails</argLine.printGC>
 * <p>
 * FastThreadLocal相关源码
 * <p>
 * 无符号右移的规则只记住一点：忽略了符号位扩展，0补最高位  无符号右移运算符>>> 只是对32位和64位的值有意义
 * >>>为无符号右移即若该数为正，则高位补0，而若该数为负数，则右移后高位同样补0)
 * <p>
 * <p>
 * 字节码计算工具--->JOL Java Object Layout
 * 推荐IDEA插件：https://plugins.jetbrains.com/plugin/10953-jol-java-object-layout
 * jdk工具 http://openjdk.java.net/projects/code-tools/jol/
 * <p>
 * <p>
 * JCTools库 无锁队列
 *
 * <p>
 * 缓存行以及netty设计疑问 https://juejin.im/post/6844903878870171662
 * </p>
 */
public class Netty1_FastThreadLocal {


    /**
     * {@link io.netty.microbench.concurrent.FastThreadLocalFastPathBenchmark}
     * {@link io.netty.microbench.concurrent.FastThreadLocalSlowPathBenchmark}
     *
     * @see io.netty.util.concurrent.FastThreadLocalTest
     */
    @Test
    public void fastThreadLocal() {
        FastThreadLocal<String> ftl = new FastThreadLocal<>();
        ftl.set("hello");
        System.out.println(ftl.get());
        ftl.remove();
        System.out.println(ftl.get());
    }

    /**
     * IdentityHashMap的构造函数:其实capacity函数已经保证了2的倍数和预分配了，按道理init里面不用乘2了。 这里乘2是因为capacity指的是key的个数， 而IdentityHashMay的存储方式是在一个Object[]中key和value相邻存储， 所以要两倍的空间大小
     */
    @Test
    public void t() {
        FastThreadLocal<Object> local = new FastThreadLocal<>();
        local.set("第一次设值成功之后会初始化remove");
        //第二次构建ftl会使自身的idx继续递增
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>().set("11");
        new FastThreadLocal<>().set("1");
        new FastThreadLocal<>();
        new FastThreadLocal<>();
        new FastThreadLocal<>();
        new FastThreadLocal<>();
        new FastThreadLocal<>();

        FastThreadLocal.removeAll();
    }

    @Test
    public void testSingleObj() {
        System.out.println(Netty1_FastThreadLocal.getInstance());

    }


    private static class SingleInstanceHolder {
        public static Netty1_FastThreadLocal instance = new Netty1_FastThreadLocal();
    }

    public static Netty1_FastThreadLocal getInstance() {
        return SingleInstanceHolder.instance;
    }

/*    private Netty1_FastThreadLocal() {

    }*/


    public Netty1_FastThreadLocal() {

    }
}