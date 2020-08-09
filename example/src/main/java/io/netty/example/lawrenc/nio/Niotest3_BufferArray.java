package io.netty.example.lawrenc.nio;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * @author : MrLawrenc
 * date  2020/8/1 18:33
 * <p>
 * 关于Buffer的Scattering和Gathering
 * <p>
 * 使用telnet或其他客户端测试
 */
public class Niotest3_BufferArray {
    @Test
    public void bufferArray() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(8899);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(socketAddress);

        int len = 2 + 3 + 4;
        ByteBuffer[] byteBuffer = new ByteBuffer[3];
        byteBuffer[0] = ByteBuffer.allocate(2);
        byteBuffer[1] = ByteBuffer.allocate(3);
        byteBuffer[2] = ByteBuffer.allocate(4);


        SocketChannel socketChannel = serverSocketChannel.accept();
        while (true) {
            int readLen = 0;
            while (readLen < len) {
                //需要阻塞io（不能socketChannel.configureBlocking(false)设为非阻塞），一直读
                long read = socketChannel.read(byteBuffer);
                readLen += read;
            }
            System.out.println("#####################");
            for (ByteBuffer buffer : byteBuffer) {
                System.out.println("buffer  read : " + new String(buffer.array()));
                System.out.println(String.format("position:%s limit:%s ", buffer.position(), buffer.limit()));
                buffer.flip();

            }
            System.out.println("#####################\n\n");

            //回写
            socketChannel.write(byteBuffer);
            Arrays.asList(byteBuffer).forEach(Buffer::clear);
        }
    }
}