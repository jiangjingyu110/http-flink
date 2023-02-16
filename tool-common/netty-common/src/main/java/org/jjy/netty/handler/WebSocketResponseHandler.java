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
package org.jjy.netty.handler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.channel.group.ChannelGroup;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.FullHttpRequest;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.websocketx.*;
import org.jjy.netty.listener.WebSocketResponseListener;

/**
 * 响应webSocket监听的默认实现
 *
 * @author 姜静宇 2023年2月16日
 */
@Data
@Slf4j
public class WebSocketResponseHandler implements WebSocketResponseListener {
    /**
     * 存储所有连接的对象
     * 可以发公共通知
     */
    private ChannelGroup group;
    /**
     * netty上下文
     */
    private ChannelHandlerContext ctx;
    /**
     * websocket握手者
     */
    private WebSocketServerHandshaker handShaker;
    /**
     * 访问的主机
     */
    private String host;
    /**
     * 访问的地址
     */
    private String uri;
    /**
     * 许可证
     */
    private String license;

    /**
     * 封装netty上下文和请求对象
     *
     * @param ctx     netty上下文
     * @param request 请求对象
     */
    public WebSocketResponseHandler(ChannelHandlerContext ctx, FullHttpRequest request) {
        //建立连接
        this.ctx = ctx;
        this.host = request.headers().get(HttpHeaderNames.HOST);
        this.uri = request.uri();
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory("ws://" + host + uri, null, false);
        handShaker = factory.newHandshaker(request);
        if (handShaker == null) {
            CharSequence version = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
            throw new RuntimeException("创建握手对象失败，不支持的websocket协议版本：" + version);
        }
        handShaker.handshake(ctx.channel(), request);

    }

    @Override
    public void send(String content) {
        //自动应答
        TextWebSocketFrame res = new TextWebSocketFrame(content);
        //这里之前时放外面的
        ctx.channel().writeAndFlush(res);
    }

    @Override
    public void close(CloseWebSocketFrame closeWebSocketFrame) {
        handShaker.close(ctx.channel(), closeWebSocketFrame);
    }

    @Override
    public void close() {
        close(new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE));
    }
}
