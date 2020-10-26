package io.netty.example.lawrenc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

/**
 * @author hz20035009-逍遥
 * date   2020/10/20 15:01
 */
public class ByteBufTest {

    @Test
    public void allocate() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(1024);
        buffer.writeBytes("dssaas".getBytes());

        byte[] bytes = new byte[1024];
        buffer.readBytes(bytes);
        System.out.println(new String(bytes));
    }


    /**
     * PooledUnsafeDirectByteBuf 源码学习
     * <p>
     * <p>
     * https://www.cnblogs.com/wuhaonan/p/11386835.html
     * <p>
     * https://www.cnblogs.com/xianyijun/p/5432884.html
     */
    public void debugStart() {
        PooledByteBufAllocator.DEFAULT.directBuffer();
    }
}