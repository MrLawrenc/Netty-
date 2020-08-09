package io.netty.example.lawrenc.nio;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;

/**
 * @author : MrLawrenc
 * date  2020/8/1 18:32
 */
public class NioTest4_Channel {
    @Test
    public void io2nio() throws Exception {
        FileInputStream inputStream = new FileInputStream(new File("NioFileTest.txt"));

        FileChannel channel = inputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        channel.read(byteBuffer);

        byteBuffer.flip();
        while (byteBuffer.remaining() > 0) {
            System.out.print((char) byteBuffer.get());
        }
        System.out.println();
        inputStream.close();


        FileOutputStream outputStream = new FileOutputStream(new File("NioFileTest.txt"));
        byteBuffer.clear();

        String addStr = "msg:" + LocalDateTime.now();
        byteBuffer.put(addStr.getBytes());
        byteBuffer.flip();

        outputStream.getChannel().write(byteBuffer);
        outputStream.close();

    }
}