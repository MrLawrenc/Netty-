package io.netty.example.lawrenc.netty.javaspi;

import org.junit.Test;

import java.util.ServiceLoader;

/**
 * 详见 META-INF/services 下的spi配置
 * <p>
 * mysql等数据库驱动加载原理就依赖于jdk的spi，和当前demo一样
 * 其他springbbot、dubbo框架的spi都是此原理
 *
 * 这篇文章很老了，但是讲的很好，可以多读几遍 https://developer.51cto.com/art/202006/619930.htm
 *
 * @author MrLawrenc
 * date  2020/11/21 22:10
 */
public class JdkSPITest {
    @Test
    public void testSayHi() throws Exception {
        ServiceLoader<Developer> serviceLoader = ServiceLoader.load(Developer.class);
        serviceLoader.forEach(c -> {
            c.sayHi();
            System.out.println(c.getClass().getClassLoader());
        });
    }
} 