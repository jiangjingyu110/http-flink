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

import org.apache.flink.shaded.netty4.io.netty.channel.ChannelHandlerContext;

/**
 * 相应监听器
 *
 * @param <T> 写入的数据类型
 * @author 姜静宇 2023年2月12日
 */
public interface HttpResponseListener<T> {
    /**
     * 写入数据事件
     *
     * @param data 数据
     */
    void write(T data);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 写入数据并关闭连接
     *
     * @param data 数据
     */
    default void writeAndClose(T data) {
        write(data);
        close();
    }

    /**
     * 获取netty中写入消息的对象
     */
    ChannelHandlerContext getCtx();

    /**
     * 结果缓存对象
     */
    StringBuilder getBuffer();
}
