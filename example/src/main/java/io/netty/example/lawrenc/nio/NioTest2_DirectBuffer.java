package io.netty.example.lawrenc.nio;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author : MrLawrenc
 * date  2020/8/1 14:57
 * <p>
 * 堆外内存相关操作
 */
public class NioTest2_DirectBuffer {

    @Test
    public void testDirectBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
    }

    /**
     * 直接内存映射，在堆外内存进行io操作
     */
    @Test
    public void testMappedByteBuffer() throws Exception {
        try (RandomAccessFile accessFile = new RandomAccessFile(new File("NioFileTest.txt"), "rw")) {
            FileChannel channel = accessFile.getChannel();

            int len = 4;

            ByteBuffer allocate = ByteBuffer.allocate(len);
            for (int i = 0; i < len; i++) {
                channel.read(allocate, i);
            }
            System.out.println("开始前:" + new String(allocate.array()));
            //映射开始的4个字节
            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4);

            mappedByteBuffer.put(0, (byte) 'c');
            mappedByteBuffer.put(2, (byte) 'd');
            for (int i = 0; i < len; i++) {
                channel.read(allocate, i);
            }
            System.out.println("映射更改之后(idea可能没刷新，文件管理器查看):" + new String(allocate.array()));
        }
    }

    //https://github.com/giantray/stackoverflow-java-top-qa/blob/master/contents/how-do-you-assert-that-a-certain-exception-is-thrown-in-junit-4-tests.md
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    //@Test(expected= IOException.class)
    @Test
    public void testChannelLock() throws Exception {
        try (RandomAccessFile accessFile = new RandomAccessFile(new File("NioFileTest.txt"), "rw")) {
            FileChannel channel = accessFile.getChannel();


            //从开始锁住4个字节，并且为非共享、排他锁
            FileLock fileLock = channel.lock(0, 4, false);

            System.out.println("shared:" + fileLock.isShared());
            System.out.println("valid:" + fileLock.isValid());

            //拥有锁 可以写
            ByteBuffer allocate = ByteBuffer.allocate(6);
            allocate.put("abcdes".getBytes());
            allocate.flip();
            channel.write(allocate);

            //没有锁无法写入 ,会抛出IO异常
            exception.expect(IOException.class);
            try (RandomAccessFile accessFile1 = new RandomAccessFile(new File("NioFileTest.txt"), "rw")) {
                FileChannel channel1 = accessFile1.getChannel();
                ByteBuffer allocate1 = ByteBuffer.allocate(6);
                allocate1.put("poiuyy".getBytes());
                allocate1.flip();
                channel1.write(allocate1);
            }
        }
    }
}