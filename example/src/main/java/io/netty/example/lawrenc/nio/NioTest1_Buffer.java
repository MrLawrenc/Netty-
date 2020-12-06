package io.netty.example.lawrenc.nio;

import org.junit.Test;

import java.nio.IntBuffer;
import java.security.SecureRandom;

/**
 * bio nio aio : https://www.cnblogs.com/twoheads/p/10712094.html
 * @author : MrLawrenc
 * date  2020/7/28 21:04
 */
public class NioTest1_Buffer {

    @Test
    public void buffer1() {
        IntBuffer buffer = IntBuffer.allocate(10);

        System.out.println(buffer.capacity() + " " + buffer.limit() + " " + buffer.position());

        for (int i = 0; i < buffer.capacity(); i++) {
            int nextInt = new SecureRandom().nextInt(10);
            buffer.put(nextInt);
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }


    /**
     * 切片产生的新buffer和源buffer共用的一个底层数组，任何一个改变都会对另一个产生影响
     */
    @Test
    public void slice() throws Exception {
        IntBuffer intBuffer = IntBuffer.allocate(10);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i);
        }

        intBuffer.position(2);
        intBuffer.limit(6);

        IntBuffer slice = intBuffer.slice();
        for (int i = 0; i < slice.capacity(); i++) {
            int old = slice.get();
            slice.put(i, old * 2);
        }

        intBuffer.position(0);
        intBuffer.limit(intBuffer.capacity());


        for (int i = 0; i < intBuffer.capacity(); i++) {
            System.out.println("source:" + intBuffer.get());
        }
    }

}