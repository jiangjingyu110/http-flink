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
package org.jjy.netty.listener;

import org.apache.flink.shaded.netty4.io.netty.channel.Channel;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * websocket监听
 *
 * @author 姜静宇 2023年2月16日
 */
public interface WebSocketListener {
    /**
     * 添加通道
     *
     * @param channel 通道
     */
    void addChannel(Channel channel);

    /**
     * 移除通道
     *
     * @param channel 通道
     */
    void removeChannel(Channel channel);

    /**
     * 写入并刷新帧
     *
     * @param frame 帧数据
     */
    void writeAndFlushFrame(WebSocketFrame frame);

    /**
     * 添加响应监听器
     *
     * @param key      监听器的唯一标识，一般使用连接id
     * @param listener 响应监听器
     */
    void putWebSocketResponseListener(String key, WebSocketResponseListener listener);

    /**
     * 获取响应监听器
     *
     * @param key 监听器的唯一标识，一般使用连接id
     * @return
     */
    WebSocketResponseListener getWebSocketResponseListener(String key);

    /**
     * 移除响应监听器
     *
     * @param key 监听器的唯一标识，一般使用连接id
     */
    void removeWebSocketResponseListener(String key);

    /**
     * 读取前端传过来的frame
     *
     * @param ctx
     * @param frame
     */
    void action(ChannelHandlerContext ctx, WebSocketFrame frame);
}
