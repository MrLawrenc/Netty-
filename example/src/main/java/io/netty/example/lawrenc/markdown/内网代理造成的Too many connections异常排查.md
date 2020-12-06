# 内网代理造成的Too many connections异常排查

- [Netty内网穿透服务](https://github.com/MrLawrenc/Netty_Proxy)
- 当前bug已在`2.1.4`版本修正

首先连上docker 看mysql容器连接数

![docker容器连不上.png]https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/docker容器连不上.png)

由于我是root用户造成的所有连接耗尽，因此没有备用连接，只得无奈重启mysql。

---

如果确实是连接过少可以临时（重启mysql失效）增加连接数```set GLOBAL max_connections=256;```或者更改配置文件永久增加

进入docker容器查看最大连接数

![](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/%E9%87%8D%E5%90%AFmysql%E5%AE%B9%E5%99%A8.png)

mysql数据库在max_connections之外，额外提供一个连接，提供给super权限用户进行故障诊断使用，所以大家在使用mysql数据库的时候，应用程序千万别用root去连接数据库，一旦发生问题，dba连看数据库性能的机会都没有了

**为了防止发生too many connections时候无法登录的问题**，mysql manual有如下的说明：

```xml
mysqld actually allows max_connections+1 clients to connect. The extra connection is reserved for use by accounts that have the SUPER privilege. By granting the SUPER privilege to administrators and not to normal users (who should not need it), an administrator can connect to the server and use SHOW PROCESSLIST to diagnose problems even if the maximum number of unprivileged clients are connected.
```

因此, 必须只 **赋予root用户的SUPER权限**，同时所有数据库连接的帐户不能赋予SUPER权限。前面说到的报错后无法登录就是由于我们的应用程序直接配置的root用户

---

在重启mysql之后，本地复现，启动netty代理mysql服务，之后本地不断的启动study服务之后再关闭，在多次之后，发现mysql连接耗尽，进入docker容器查看mysql

![](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/image.png)

发现已经关闭的服务连接仍然存在，没有释放

![](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/bb.png)

因此，每启动一次study所占用的连接都会递增，渐渐的就导致了mysql连接耗尽的情况，初步判断肯定是netty代理服务的bug

找到netty内网代理客户端进程，```pstree -p port```查看该进程下的所有线程

![进程下挂着大量的内网代理客户端线程未释放](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A//aa.png)

发现大量代理客户端线程开启，且长时间未回收（已超过核心coreSize），初步断定就是内网代理客户端的连接未释放导致的

继续跟到netty代理服务端断开代码的逻辑，发现带外网代理服务端断开连接之后未通知内网代理客户端，从而内网客户端一直持有已经建立的长连接，无法释放。

修正外网代理服务端，有客户端断开之后通知内网代理客户端

```java
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        ServerManager.USER_CLIENT_MAP.remove(channelId, ctx);

        log.debug("proxy server({}) actively disconnect", ctx.channel());
        //fix mysql too many connection异常. 通知代理客户端可以断开了，否则客户端连接会一直占用
        Message message = new Message();
        MessageHeader header = message.getHeader();
        header.setType(MessageType.DISCONNECTED);
        header.setChannelId(channelId);
        clientCtx.writeAndFlush(message);

        super.channelInactive(ctx);
    }
```

内网客户端收到消息之后断开相应的代理客户端

```java
    /**
     * 与代理服务端连接的用户客户端断开连接，处理资源，以及断开内网代理客户端
     */
    private void processDisconnected(Channel channel, Message message) {
        ChannelHandlerContext context = ClientManager.ID_SERVICE_CHANNEL_MAP.get(message.getHeader().getChannelId());
        if (Objects.nonNull(context)) {
            context.close();
            ClientManager.removeChannelMapByProxyClient(channel, message.getHeader().getChannelId());
        }
    }
```

---

之后再次测试，启动study服务

![image-20201205223619897](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/image-20201205223619897.png)

关闭服务

![image-20201205223638089](https://lmy25.wang/Netty%E5%9B%BE%E5%BA%8A/image-20201205223638089.png)

连接已释放