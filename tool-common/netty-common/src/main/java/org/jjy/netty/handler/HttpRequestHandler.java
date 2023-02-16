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

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.channel.SimpleChannelInboundHandler;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.*;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jjy.netty.listener.HttpListener;
import org.jjy.netty.listener.WebSocketListener;

import java.util.Calendar;
import java.util.UUID;

/**
 * http处理类
 *
 * @author 姜静宇 2023年2月16日
 */
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * http监听器
     */
    private final HttpListener httpListener;
    /**
     * webSocket监听器
     */
    private final WebSocketListener webSocketListener;

    /**
     * 通过http和webSocket的监听器，构造http处理类
     *
     * @param httpListener      http监听器
     * @param webSocketListener webSocket监听器
     */
    public HttpRequestHandler(HttpListener httpListener, WebSocketListener webSocketListener) {
        this.httpListener = httpListener;
        this.webSocketListener = webSocketListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            httpChannelRead(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            webSocketChannelRead(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 读取http
     *
     * @param ctx     netty上下文
     * @param request 请求对象
     */
    private void httpChannelRead(ChannelHandlerContext ctx, FullHttpRequest request) {
        //如果是websocket的升级请求，则按websocket升级方式处理
        if (request.decoderResult().isSuccess() && "websocket".equals(request.headers().get(HttpHeaderNames.UPGRADE))) {
            //使用channelId来关联websocket响应处理类
            String channelId = ctx.channel().id().asLongText();
            webSocketListener.putWebSocketResponseListener(channelId, new WebSocketResponseHandler(ctx, request));
            return;
        }
        //处理普通的http请求
        //100 Continue含义
        //HTTP客户端程序有一个实体的主体部分要发送给服务器，但希望在发送之前查看下服务器是否会接受这个实体，所以在发送实体之前先发送了一个携带100 Continue的Expect请求首部的请求。
        //服务器在收到这样的请求后，应该用 100 Continue或一条错误码来进行响应。
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }
        //先添加监听，后触发事件
        //生成一个连接id，用来关联request和response
        String connectId = UUID.randomUUID().toString();
        httpListener.putHttpResponseListener(connectId, new HttpResponseHandler(ctx));
        //同步调用，需要等flink处理后再返回
        httpListener.action(request, connectId, Calendar.getInstance().getTimeInMillis());
    }

    /**
     * 读取websocket
     *
     * @param ctx   netty上下文
     * @param frame 帧数据
     */
    private void webSocketChannelRead(ChannelHandlerContext ctx, WebSocketFrame frame) {
        webSocketListener.action(ctx, frame);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端[{}]与服务器建立连接", ctx.channel().id().asShortText());
        webSocketListener.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端[{}]与服务器断开连接", ctx.channel().id().asShortText());
        webSocketListener.removeChannel(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
