/**
 * Copyright 2023 姜静宇(jiangjingyu110@163.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jjy.netty.server;

import org.apache.flink.shaded.netty4.io.netty.bootstrap.ServerBootstrap;
import org.apache.flink.shaded.netty4.io.netty.channel.*;
import org.apache.flink.shaded.netty4.io.netty.channel.nio.NioEventLoopGroup;
import org.apache.flink.shaded.netty4.io.netty.channel.socket.SocketChannel;
import org.apache.flink.shaded.netty4.io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpObjectAggregator;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpServerCodec;
import org.apache.flink.shaded.netty4.io.netty.handler.stream.ChunkedWriteHandler;
import org.jjy.netty.handler.HttpRequestHandler;
import org.jjy.netty.listener.HttpListener;
import org.jjy.netty.listener.WebSocketListener;

/**
 * netty实现httpServer
 *
 * @author 姜静宇 2022年5月6日
 */
public class HttpServer {
    /**
     * 端口
     */
    private final int port;
    /**
     * http监听器
     */
    private final HttpListener httpListener;
    /**
     * webSocket监听器
     */
    private final WebSocketListener webSocketListener;
    /**
     * 服务器接收线程
     * 用于处理服务器端接收客户端连接
     */
    private EventLoopGroup bossGroup;
    /**
     * 进行通信的线程（读写）
     */
    private EventLoopGroup workerGroup;

    public HttpServer(int port, HttpListener httpListener, WebSocketListener webSocketListener) {
        this.port = port;
        this.httpListener = httpListener;
        this.webSocketListener = webSocketListener;
    }

    /**
     * 启动服务，此方法会卡住，适合放到source中
     */
    public void run() {
        bossGroup = new NioEventLoopGroup(); //用于处理服务器端接收客户端连接
        workerGroup = new NioEventLoopGroup(); //进行网络通信（读写）
        try {
            ServerBootstrap bootstrap = new ServerBootstrap(); //辅助工具类，用于服务器通道的一系列配置
            bootstrap.group(bossGroup, workerGroup) //绑定两个线程组
                    .channel(NioServerSocketChannel.class) //指定NIO的模式
                    .childHandler(new ChannelInitializer<SocketChannel>() { //配置具体的数据处理方式
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //http编解码
                            pipeline.addLast("http-codec", new HttpServerCodec());
                            //http消息聚合器
                            pipeline.addLast("http-aggregator", new HttpObjectAggregator(512 * 1024));
                            //http块写入写出
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                            pipeline.addLast(new HttpRequestHandler(httpListener, webSocketListener));
                        }
                    })
                    /**
                     * 对于ChannelOption.SO_BACKLOG的解释：
                     * 服务器端TCP内核维护有两个队列，我们称之为A、B队列。客户端向服务器端connect时，会发送带有SYN标志的包（第一次握手），服务器端
                     * 接收到客户端发送的SYN时，向客户端发送SYN ACK确认（第二次握手），此时TCP内核模块把客户端连接加入到A队列中，然后服务器接收到
                     * 客户端发送的ACK时（第三次握手），TCP内核模块把客户端连接从A队列移动到B队列，连接完成，应用程序的accept会返回。也就是说accept
                     * 从B队列中取出完成了三次握手的连接。
                     * A队列和B队列的长度之和就是backlog。当A、B队列的长度之和大于ChannelOption.SO_BACKLOG时，新的连接将会被TCP内核拒绝。
                     * 所以，如果backlog过小，可能会出现accept速度跟不上，A、B队列满了，导致新的客户端无法连接。要注意的是，backlog对程序支持的
                     * 连接数并无影响，backlog影响的只是还没有被accept取出的连接
                     */
                    .option(ChannelOption.SO_BACKLOG, 128) //设置TCP缓冲区
                    .childOption(ChannelOption.SO_KEEPALIVE, true); //保持连接
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭netty
     */
    public void close() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
