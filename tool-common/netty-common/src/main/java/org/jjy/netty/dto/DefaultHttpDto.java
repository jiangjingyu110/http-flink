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

import lombok.Data;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpMethod;
import org.jjy.core.flink.dto.FlinkDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * http传输对象的抽象实现
 * 大部分情况下直接继承这个类
 *
 * @param <T> 传输的数据类型
 * @author 姜静宇 2023年2月12日
 */
@Data
public class DefaultHttpDto<T> implements HttpDto<T>, FlinkDto {
    /**
     * 连接id
     */
    private String connectId;
    /**
     * 请求的头部信息
     */
    private final Map<String, String> headers;
    /**
     * 访问方法
     */
    private HttpMethod method;
    /**
     * 访问地址
     */
    private String uri;
    /**
     * 端口
     */
    private int port;
    /**
     * 传输过来的数据
     */
    private T data;
    /**
     * 额外的附加信息，用于程序处理过程中传递数据
     */
    private final Map<String, Object> extra;
    /**
     * 用于进行keyBy的值
     */
    private String keyByValue;

    public DefaultHttpDto(T data) {
        headers = new HashMap<>();
        extra = new HashMap<>();
        this.data = data;
    }

    @Override
    public String getHead(String key) {
        return headers.get(key);
    }

    @Override
    public String setHead(String key, String value) {
        return headers.put(key, value);
    }

    @Override
    public Set<String> getHeadsKey() {
        return headers.keySet();
    }

    @Override
    public void clearHead() {
        headers.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getExtra(String key) {
        return (E) extra.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E setExtra(String key, E value) {
        return (E) extra.put(key, value);
    }

    @Override
    public void clearExtra() {
        extra.clear();
    }

    @Override
    public Set<String> getExtraKey() {
        return extra.keySet();
    }

    @Override
    public String getLicense() {
        return headers.get(StatefulDto.LICENSE_KEY);
    }

    @Override
    public void clear() {
        clearExtra();
        clearHead();
        setMethod(null);
        setUri(null);
    }
}
