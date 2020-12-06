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
 * <p>
 * 二者的不同点在于“>>”在执行右移操作时，若参与运算的数字为正数，则在高位补0；若为负数，则在高位补1。而“>>>”则不同，无论参与运算的数字为正数或为负数，在执运算时，都会在高位补0。
 * <p>
 * 左移运算没有有符号和无符号左移动，在左移时，移除高位的同时在低位补0。
 * </p>
 */
@SuppressWarnings("all")
public class FastThreadLocalTest {

    @Test
    public void resizeTest() {
        int index = 7;

        int newCapacity = index;
        newCapacity |= newCapacity >>> 1;
        newCapacity |= newCapacity >>> 2;
        newCapacity |= newCapacity >>> 4;
        newCapacity |= newCapacity >>> 8;
        newCapacity |= newCapacity >>> 16;
        newCapacity++;

        System.out.println(newCapacity);
    }

    @Test
    public void moveTest() {
        System.out.println(8 >>> 1);
        System.out.println(8 >> 1);
        /**
         * <pre>
         *     对于带符号右移,若为负数,则在存储时首位表示符号位,其值为1,表示该值是负数的移位,在移位过程中,高位补1,若符号位是0,表示是正数,在移位过程中高位补零,两者的前提是符号位保持不变:
         *     对于负数的右移：因为负数在内存中是以补码形式存在的，所有首先根据负数的原码求出负数的补码(符号位不变，其余位按照原码取反加1)，然后保证符号位不变，其余位向右移动到X位，在移动的过程中，高位补1.等移位完成以后，然后保持符号位不变，其余按位取反加1，得到移位后所对应数的原码。即为所求。
         *        举例1：
         *               -100带符号右移4位。
         *               -100原码：   10000000    00000000    00000000   01100100
         *               -100补码：    保证符号位不变，其余位置取反加1
         *                           11111111    11111111    11111111   10011100
         *               右移4位   ：   在高位补1
         *                           11111111    11111111    11111111    11111001
         *                补码形式的移位完成后，结果不是移位后的结果，要根据补码写出原码才是我们所求的结果。其方法如下:
         *                保留符号位，然后按位取反
         *                           10000000    00000000    00000000     00000110
         *                然后加1，即为所求数的原码：
         *                           10000000    00000000    00000000    00000111
         *                     所有结果为：-7
         *               例2：
         *                 -100无符号右移4位。
         *               -100原码：   10000000    00000000    00000000   01100100
         *               -100补码：    保证符号位不变，其余位置取反加1
         *                           11111111    11111111    11111111   10011100
         *               无符号右移4位   ：   在高位补0
         *                           00001111    11111111    11111111    11111001
         *                即为所求：268435449
         *
         * 正数的左移与右移，负数的无符号右移，就是相应的补码移位所得，在高位补0即可。
         * 负数的右移，就是补码高位补1,然后按位取反加1即可。
         * </pre>
         */
        System.out.println(-100 >>> 4);
        System.out.println(-100 >> 4);

        System.out.println("============");
        System.out.println(8 << 1);
        System.out.println(-8 << 1);

    }

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
    public void multipleFtl() {
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
        System.out.println(FastThreadLocalTest.getInstance());

    }


    private static class SingleInstanceHolder {
        public static FastThreadLocalTest instance = new FastThreadLocalTest();
    }

    public static FastThreadLocalTest getInstance() {
        return SingleInstanceHolder.instance;
    }

/*    private FastThreadLocalTest() {

    }*/


    public FastThreadLocalTest() {

    }
}