package io.netty.example.lawrenc.netty.memoryleak;

/**
 * ThreadLocal模拟tomcat6的内存泄漏。threadLocal泄漏，导致classloader部分类无法卸载，在多次热部署之后造成方法区oom
 * <p>
 * 参考文章
 * 1.探究tomcat内存泄漏的一篇文章---MemoryLeakProtection<url>https://cwiki.apache.org/confluence/display/tomcat/MemoryLeakProtection?spm=a2c6h.12873639.0.0.3d661681z7SW2e</url>
 * 2.https://developer.aliyun.com/article/689657
 * </p>
 *
 * @author MrLawrenc
 * date  2020/11/20 21:37
 */
public class MemoryLeak {

    public static void main(String[] args) throws Exception {
       /* new Thread(() -> {
            try {
                loadFoo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();*/

      /*  CompletableFuture.runAsync(()->{
            try {
                loadFoo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/

        loadFoo();

        while (true) {
            System.gc();
            Thread.sleep(1000);
        }
    }

    private static void loadFoo() throws Exception {
        CustomClassLoader cl = new CustomClassLoader("F:\\openSources\\netty\\example\\target\\classes\\io\\netty\\example\\lawrenc\\netty");
        Class<?> clazz = cl.loadClass("io.netty.example.lawrenc.netty.memoryleak.Foo");
        clazz.newInstance();
        cl = null;
    }
}