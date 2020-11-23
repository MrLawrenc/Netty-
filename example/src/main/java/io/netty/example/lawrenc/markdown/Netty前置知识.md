## Netty

## 网络

### 网络概述

- 访问互联网首先连接局域网，简称LAN，之后局域网访问广域网，简称WAN，WAN的路由器一般属于“互联网服务提供商”，简称ISP。

  之后小的WAN会连接到更大的WAN，可能会覆盖城市，之后再跳若干次会到达互联网主干。

  如下是访问个人站的流程（windows系统）

  ![image-20200727222558621](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200727222558621.png)

- 数据包在互联网传输要符合“互联网协议”的标准，即IP。数据包过大会被分为若干个小包发送。

### 协议

- IP协议

  ![image-20200727223133441](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200727223133441.png)

  数据包的头部只有目标地址，意味着数据包只知道到达某台电脑，并不知道将数据包给到电脑的哪个端口，即程序。因此需要在IP协议之上开发其他协议，如TCP/IP、UDP等。

- 用户数据报协议-UDP

  ![image-20200727223717213](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200727223717213.png)

  该协议在也有头部，头部位于数据之前，里面包含诸多信息，之一就是端口号，每个想访问网络的应用程序都会向操作系统申请一个端口号，如mysql会申请3306端口。当一个数据包到达时，接收方会读UDP头，根据端口号将数据包给到对应的程序。

  IP 负责把数据包送到正确的计算机

  UDP 负责把数据包送到正确的程序

  UDP头部还包含校验和，用于检查数据是否正确。如UDP传输的数据包为1 、2 、3，发送前会计算校验和（16位存储，若超过，高位数被扔掉，保留低位），将结果6保存到头部CHECKSUM。当接收方收到数据包时，会重复进行如上操作计算校验和，并和头部校验和比对，一致则代表一切正常，不一致则代表数据不完整，数据包通常会直接丢掉（UDP不提供数据修复或重发的机制）。并且UDP无法感知数据包是否到达，由于轻便、快速常用于视频等领域。

- 传输控制协议-TCP

  TCP协议要求数据必须到达。

  ![image-20200727224805057](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200727224805057.png)

  TCP同UDP一样，都有端口号和校验和，且还具备更多高级功能。

  TCP数据包有序号（UDP无序，若要有序，需要自定义协议组包），当一个数据包太大被拆分多次发送时，即使接收方数据包到达的时间不同，TCP也能将到达乱序的数据包顺序排对。

  TCP要求接收方“校验和”检查无误后，给发送方回一个确认码，代表数据收到了。若过了一段时间没有收到确认码，发送发会再发一次，当某一个包延迟很久到达，而TCP又发送了第二次数据包时，由于有序号，接收方会将收到的重复数据包删掉。TCP也能同时发多个数据包，可以通过确认码和发包数量感知到网络拥堵情况，并以此来调整每次同时发包的数量，最终解决拥堵和效率问题。

### 缓存行




## IO概述

- BIO-阻塞IO

  一个连接绑定一个线程，当连接数较高时耗费资源多，效率低。但是在低连接数，并发小的时候BIO性能不输NIO，且BIO编码简单。

- NIO-非阻塞IO

  非阻塞IO，操作系统的实现由select、poll、epoll函数。

- AIO-异步IO

- Netty的选择-NIO（没有选择AIO）

  - windows对于AIO实现成熟，但是极少作为服务器
  - linux虽说作为服务器较多，但是对于AIO实现不够成熟
  - linux下AIO对比NIO的性能提升不明显

- 

### 



## NIO

java原始io中是面向流编程，一个流要么是输出流，要么是输入流，不可能同时具备输入输出流的特性。但是在nio中，是面向块（block）或者是缓冲区（buffer）编程的，所有的数据由channel读到buffer。channel是双向的，所以既可以读也可以写，也能更好的反映操作系统的实际情况（linux中的通道也是双向的）。buffer其本身是一块内存（byte数组），nio由Selecter、Channel、Buffer构成

### Buffer

java中数据类型基本都有对应的buffer实现(如IntBuffer等类是由机器生成的)

![image-20200728211220813](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200728211220813.png)

如下是一个buffer小示例，程序会输出随机的10个整数

```java
 public static void main(String[] args) {
     IntBuffer buffer = IntBuffer.allocate(10);

     for (int i = 0; i < buffer.capacity(); i++) {
         int nextInt = new SecureRandom().nextInt(10);
         buffer.put(nextInt);
     }

     buffer.flip();

     while (buffer.hasRemaining()) {
         System.out.println(buffer.get());
     }
 }
```

---

Buffer中较为重要的三个成员

```java
private int position = 0;
private int limit;
private int capacity;
```

**capacity代表缓冲区容量，一旦分配之后则不会改变。**

**limit代表读或写索引的上限，不能超过的索引位置，因此limit满足 0=< limit <=capacity**

**position代表下一个将要被读或写的元素的索引，满足0 <= position <=limit**

Buffer初始状态如下（若开辟一个容量为6的buffer(IntBuffer.allocate(10))）(position=0,limit=capacity=6)

![image-20200728222655020](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200728222655020.png)

随后写入两条数据1和2，状态改变为如下(position=2,limit=capacity=6)

![image-20200728222944791](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200728222944791.png)

之后由读状态切换为写状态，调用flip方法，flip方法如下（注意原来写入的数据仍然存在）

```java
public Buffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
}
```

之后状态改变为如下（position=0,limit=2，capacity=6）

![image-20200728223453158](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200728223453158.png)

随后写入一个数据（position=1,limit=2，capacity=6）

![image-20200728224140180](C:\Users\Liu Mingyao\AppData\Roaming\Typora\typora-user-images\image-20200728224140180.png)

由以上写的过程可以看出Buffer本质就是在改变position和limit的值，并以此来控制我们写入数据的位置，而且在flip之后并不会擦除我们已存在的数据。Buffer的读取数据和写的过程一致，因此在使用过程中，若存在状态改变（读写切换），则必须调用flip方法，否则读取到的数据错误甚至会抛出异常。

---

所有的Buffer都提供了相对操作和绝对操作，简单理解就是相对操作会改变position，而绝对操作不影响position。

mark和reset操作可以实现数据重复读，源码如下（记录position值和恢复position值）

```java
//默认值
private int mark = -1;

public Buffer mark() {
    mark = position;
    return this;
}

public Buffer reset() {
    int m = mark;
    if (m < 0)
        throw new InvalidMarkException();
    position = m;
    return this;
}
```

Buffer中始终满足0 <= mark <= position <= limit <= capacity关系

clear（归为初始状态）、remaining（常用来判断是否可读、写）、rewind源码如下。

```java
public Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
}

public final int remaining() {
    return limit - position;
}

public Buffer rewind() {
    position = 0;
    mark = -1;
    return this;
}
```

其余还有slice切片操作,类似于将源buffer复制一份，但是底层是同一份数据，更改任何一份数据都会引起另一份更改。

```java
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
```





### Channel

channel指的是可以向其写入或是从中读取数据的对象，类似于io中的流Stream。所有数据都是经过buffer来进行的，不会直接向channel读写数据（和go的channel有点类似，但是go能直接从channel中读写）。

如下将传统io转换为nio，以文件操作为例

```java
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
```

### Selecter

IO多路复用模型(**I/O multiplexing**)，就是通过一种新的系统调用，一个进程可以监视多个文件描述符("一切皆文件")，一旦某个描述符就绪（一般是内核缓冲区可读/可写），内核kernel能够通知程序进行相应的IO系统调用。
目前支持IO多路复用的系统调用，有poll， select，epoll等等。select系统调用，是目前几乎在所有的操作系统上都有支持，具有良好跨平台特性。epoll是在linux 2.6内核中提出的，是select系统调用的linux增强版本。
IO多路复用模型的基本原理就是select/epoll系统调用，单个线程不断的轮询select/epoll系统调用所负责的成百上千的socket连接，当某个或者某些socket网络连接有数据到达了，就返回这些可以读写的连接。因此，好处也就显而易见了——通过一次select/epoll系统调用，就查询到到可以读写的一个甚至是成百上千的网络连接。
举个栗子。发起一个多路复用IO的的read读操作系统调用，流程是这个样子： 

![image](https://lmy25.wang/upload/2020/08/image-03d4e29c2eae4451a4fa3717c2f3458e.png)

在这种模式中，首先不是进行read系统调动，而是进行select/epoll系统调用。当然，这里有一个前提，需要将目标网络连接，提前注册到select/epoll的可查询socket列表中。然后，才可以开启整个的IO多路复用模型的读流程。

（1）进行select/epoll系统调用，查询可以读的连接。kernel会查询所有select的可查询socket列表，当任何一个socket中的数据准备好了，select就会返回。
当用户进程调用了select，那么整个线程会被block（阻塞掉）。
（2）用户线程获得了目标连接后，发起read系统调用，用户线程阻塞。内核开始复制数据。它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存），然后kernel返回结果。
（3）用户线程才解除block的状态，用户线程终于真正读取到数据，继续执行。

多路复用IO的特点：
IO多路复用模型，建立在操作系统kernel内核能够提供的多路分离系统调用select/epoll基础之上的。多路复用IO需要用到两个系统调用（system call）， 一个select/epoll查询调用，一个是IO的读取调用。
和NIO模型相似，多路复用IO需要轮询。负责select/epoll查询调用的线程，需要不断的进行select/epoll轮询，查找出可以进行IO操作的连接。
另外，多路复用IO模型与前面的NIO模型，是有关系的。对于每一个可以查询的socket，一般都设置成为non-blocking模型。只是这一点，对于用户程序是透明的（不感知）。

多路复用IO的优点：
用select/epoll的优势在于，它可以同时处理成千上万个连接（connection）。与一条线程维护一个连接相比，I/O多路复用技术的最大优势是：系统不必创建线程，也不必维护这些线程，从而大大减小了系统的开销。
Java的NIO（new IO）技术，使用的就是IO多路复用模型。在linux系统上，使用的是epoll系统调用。
多路复用IO的缺点：

本质上，select/epoll系统调用，属于同步IO，也是阻塞IO。都需要在读写事件就绪后，自己负责进行读写，也就是说这个读写过程是阻塞的。

- AIO扩展

  如何进一步提升效率，解除最后一点阻塞呢？这就是异步IO模型，全称asynchronous I/O，简称为AIO。
  AIO的基本流程是：用户线程通过系统调用，告知kernel内核启动某个IO操作，用户线程返回。kernel内核在整个IO操作（包括数据准备、数据复制）完成后，通知用户程序，用户执行后续的业务操作。
  kernel的数据准备是将数据从网络物理设备（网卡）读取到内核缓冲区；kernel的数据复制是将数据从内核缓冲区拷贝到用户程序空间的缓冲区。

  ![image](https://lmy25.wang/upload/2020/08/image-8a104f1021a04a05a26b603143d3b7d0.png)

  （1）当用户线程调用了read系统调用，立刻就可以开始去做其它的事，用户线程不阻塞。
  （2）内核（kernel）就开始了IO的第一个阶段：准备数据。当kernel一直等到数据准备好了，它就会将数据从kernel内核缓冲区，拷贝到用户缓冲区（用户内存）。
  （3）kernel会给用户线程发送一个信号（signal），或者回调用户线程注册的回调接口，告诉用户线程read操作完成了。
  （4）用户线程读取用户缓冲区的数据，完成后续的业务操作。

  异步IO模型的特点：
  在内核kernel的等待数据和复制数据的两个阶段，用户线程都不是block(阻塞)的。用户线程需要接受kernel的IO操作完成的事件，或者说注册IO操作完成的回调函数，到操作系统的内核。所以说，异步IO有的时候，也叫做信号驱动 IO 。
  
  异步IO模型缺点：
  需要完成事件的注册与传递，这里边需要底层操作系统提供大量的支持，去做大量的工作。
  目前来说， Windows 系统下通过 IOCP 实现了真正的异步 I/O。但是，就目前的业界形式来说，Windows 系统，很少作为百万级以上或者说高并发应用的服务器操作系统来使用。
  而在 Linux 系统下，异步IO模型在2.6版本才引入，目前并不完善。所以，这也是在 Linux 下，实现高并发网络编程时都是以 IO 复用模型模式为主。

---

如下为一个Selector的简单示例。服务器开辟多个端口，向seletor注册SelectionKey.OP_ACCEPT事件（即一个连接建立的事件），之后一直循环获取seletor返回的key，当有返回时，代表当前连接建立成功，则再向seletor注册一个读SelectionKey.OP_READ事件，当读就绪时，会返回相应的SocketChannel进行数据读取。

```java
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
```

