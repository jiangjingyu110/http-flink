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
package org.jjy.netty.dto;

import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpMethod;

import java.util.Set;

/**
 * http传输对象通用接口
 * 可以认为无状态的请求就不需要用flink来处理
 *
 * @param <T> 用来传递的数据类型
 * @author 姜静宇 2023年2月12日
 */
public interface HttpDto<T> extends StatefulDto {
    /**
     * 获取连接id
     *
     * @return 连接id
     */
    String getConnectId();

    /**
     * 设置连接id
     *
     * @param connectId 连接id
     */
    void setConnectId(String connectId);

    /**
     * 获取额外数据信息
     *
     * @param key 额外数据中的key
     * @return 额外数据中的值
     */
    <E> E getExtra(String key);

    /**
     * 设置额外信息
     *
     * @param key   额外数据中的key
     * @param value 需要设置的值
     * @return 额外数据中原来的值
     */
    <E> E setExtra(String key, E value);

    /**
     * 清理额外信息
     */
    void clearExtra();

    /**
     * 获取所有的额外数据key
     *
     * @return 所有的额外数据key
     */
    Set<String> getExtraKey();

    /**
     * 获取请求头信息
     *
     * @param key 请求头中的key
     * @return 请求头中的值
     */
    String getHead(String key);

    /**
     * 设置请求头信息
     *
     * @param key   请求头中的key
     * @param value 请求头中的值
     * @return 请求头中原来的值
     */
    String setHead(String key, String value);

    /**
     * 清理请求头信息
     */
    void clearHead();

    /**
     * 获取请求头中所有的key
     *
     * @return 所有的key
     */
    Set<String> getHeadsKey();

    /**
     * 获取请求方式
     *
     * @return 请求方式
     */
    HttpMethod getMethod();

    /**
     * 设置请求方式
     *
     * @param method 请求方式
     */
    void setMethod(HttpMethod method);

    /**
     * 获取请求地址
     *
     * @return 请求地址
     */
    String getUri();

    /**
     * 设置请求地址
     *
     * @param uri 请求地址
     */
    void setUri(String uri);

    /**
     * 获取端口
     *
     * @return 端口
     */
    int getPort();

    /**
     * 设置端口
     *
     * @param port 端口
     */
    void setPort(int port);

    /**
     * 获取数据
     *
     * @return 数据
     */
    T getData();

    /**
     * 设置数据
     *
     * @param data 数据
     */
    void setData(T data);

    /**
     * 清理除连接以外的信息
     */
    void clear();

    /**
     * 设置分组键
     * 适用于flink的特色方法
     *
     * @param keyByValue 分组键
     */
    void setKeyByValue(String keyByValue);

    /**
     * 获取分组键
     * 适用于flink的特色方法
     *
     * @return 分组键
     */
    String getKeyByValue();
}
