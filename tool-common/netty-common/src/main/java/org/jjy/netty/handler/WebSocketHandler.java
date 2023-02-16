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
import org.apache.flink.shaded.netty4.io.netty.channel.Channel;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.channel.group.ChannelGroup;
import org.apache.flink.shaded.netty4.io.netty.channel.group.DefaultChannelGroup;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.websocketx.*;
import org.apache.flink.shaded.netty4.io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.jjy.netty.dto.HttpDto;
import org.jjy.netty.dto.StatefulDto;
import org.jjy.netty.dto.StringHttpDto;
import org.jjy.netty.listener.WebSocketListener;
import org.jjy.netty.listener.WebSocketResponseListener;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket处理类
 *
 * @author 姜静宇 2023年2月16日
 */
@Data
@Slf4j
public class WebSocketHandler implements WebSocketListener {
    /**
     * flink流
     */
    private SourceFunction.SourceContext<HttpDto<String>> ctx;
    /**
     * 存储所有连接的对象
     */
    private final ChannelGroup group;
    /**
     * 响应监听器
     */
    private final Map<String, WebSocketResponseListener> listenerMap;
    /**
     * 单例对象
     */
    private static WebSocketHandler webSocketHandler;

    private WebSocketHandler() {
        group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        listenerMap = new HashMap<>();
    }

    /**
     * 获取httpHandler对象
     *
     * @return 单例的httpHandler对象
     */
    public static WebSocketHandler getWebSocketHandler() {
        if (webSocketHandler != null) {
            return webSocketHandler;
        }
        synchronized (HttpHandler.class) {
            if (webSocketHandler != null) {
                return webSocketHandler;
            }
            webSocketHandler = new WebSocketHandler();
            return webSocketHandler;
        }
    }

    @Override
    public void addChannel(Channel channel) {
        group.add(channel);
    }

    @Override
    public void removeChannel(Channel channel) {
        group.remove(channel);
    }

    @Override
    public void writeAndFlushFrame(WebSocketFrame frame) {
        group.writeAndFlush(frame);
    }

    @Override
    public void putWebSocketResponseListener(String key, WebSocketResponseListener listener) {
        listenerMap.put(key, listener);
    }

    @Override
    public WebSocketResponseListener getWebSocketResponseListener(String key) {
        return listenerMap.get(key);
    }

    @Override
    public void removeWebSocketResponseListener(String key) {
        listenerMap.remove(key);
    }

    /**
     * 通过channel获取id
     *
     * @param channel
     * @return
     */
    private String getId(Channel channel) {
        return channel.id().asLongText();
    }

    @Override
    public void action(ChannelHandlerContext context, WebSocketFrame frame) {
        WebSocketResponseListener listener = listenerMap.get(getId(context.channel()));
        if (listener == null) {
            return;
        }
        //接收到关闭帧
        if (frame instanceof CloseWebSocketFrame) {
            listener.close((CloseWebSocketFrame) frame.retain());
            return;
        }
        //接收到ping，返回pong
        if (frame instanceof PingWebSocketFrame) {
            context.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //校验是否未可以处理的帧
        if (!(frame instanceof TextWebSocketFrame)) {
            log.error("不支持的二进制消息：{}", frame.getClass());
            return;
        }
        if (ctx != null) {
            //向后传递body中的信息
            ctx.collect(createData(context, frame, listener));
        }
    }

    /**
     * 通过帧创建数据
     *
     * @param context  websocket上下文
     * @param frame    一个帧的数据
     * @param listener websocket响应监听器
     * @return
     */
    private StringHttpDto createData(ChannelHandlerContext context, WebSocketFrame frame, WebSocketResponseListener listener) {
        String connectId = context.channel().id().asLongText();
        String content = ((TextWebSocketFrame) frame).text();
        StringHttpDto httpDto = new StringHttpDto(content);
        httpDto.setConnectId(connectId);
        httpDto.setHead(StatefulDto.LICENSE_KEY, listener.getLicense());
        //表示数据是websocket的数据
        httpDto.setHead(HttpHeaderNames.UPGRADE.toString(), "websocket");
        //其他header信息再说，websocket不存在method
        httpDto.setMethod(null);
        httpDto.setUri(listener.getUri());
        return httpDto;
    }
}
