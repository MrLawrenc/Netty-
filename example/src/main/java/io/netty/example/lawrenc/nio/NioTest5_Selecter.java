package io.netty.example.lawrenc.nio;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * @author : MrLawrenc
 * date  2020/8/1 18:29
 */
public class NioTest5_Selecter {

    @Test
    public void test1() throws Exception {
        Selector selector = Selector.open();
        for (Integer port : Arrays.asList(8001, 8002, 8003, 8004)) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("监听端口:" + port);
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (true) {
            int select = selector.select();
            System.out.println("selector num:" + select);
            //错误用法 会找出所有注册的key，包含未连接上的
            //Set<SelectionKey> keys = selector.keys();
            Set<SelectionKey> keys = selector.selectedKeys();
            System.out.println("selectedKeys num:" + keys.size());
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isConnectable()) {
                    System.out.println("++++++++++++++++++++++");
                }

                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();

                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    System.out.println("获取到客户端连接:" + socketChannel);
                    iterator.remove();
                } else if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();

                    if (channel.isOpen()) {
                        System.out.println("++++++++++++++++");
                    }
                    if (channel.isConnected()){
                        System.out.println("==================");
                    }

                    channel.read(byteBuffer);
                    System.out.println("读取到数据:" + new String(byteBuffer.array()));
                    byteBuffer.clear();
                    iterator.remove();
                }


            }
        }

    }

}