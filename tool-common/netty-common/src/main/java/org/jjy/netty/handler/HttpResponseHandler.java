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
import org.apache.flink.shaded.netty4.io.netty.buffer.Unpooled;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelFutureListener;
import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.*;
import org.apache.flink.shaded.netty4.io.netty.util.CharsetUtil;
import org.jjy.netty.listener.HttpResponseListener;

/**
 * 响应监听器的默认实现
 *
 * @author 姜静宇 2023年2月16日
 */
@Data
public class HttpResponseHandler implements HttpResponseListener<String> {
    /**
     * netty中写入消息的对象
     */
    private ChannelHandlerContext ctx;
    /**
     * 结果缓存对象
     */
    private StringBuilder buffer;

    /**
     * 封装netty中写入消息的对象
     * @param ctx netty中写入消息的对象
     */
    public HttpResponseHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.buffer = new StringBuilder();
    }

    /**
     * 通过监听器来构造另一个监听器
     *
     * @param handler
     */
    public HttpResponseHandler(HttpResponseListener<?> handler) {
        this.ctx = handler.getCtx();
        this.buffer = handler.getBuffer();
    }

    @Override
    public void write(String data) {
        buffer.append(data);
    }

    @Override
    public void close() {
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(buffer.toString(), CharsetUtil.UTF_8));
        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
