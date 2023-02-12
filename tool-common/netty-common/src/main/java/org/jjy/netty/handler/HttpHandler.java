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
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.lang3.CharEncoding;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.FullHttpRequest;
import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpHeaders;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.jjy.netty.dto.HttpDto;
import org.jjy.netty.dto.StatefulDto;
import org.jjy.netty.dto.StringHttpDto;
import org.jjy.netty.listener.HttpListener;
import org.jjy.netty.listener.HttpResponseListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * http处理类
 *
 * @author 姜静宇 2023年2月12日
 */
@Slf4j
@Data
public class HttpHandler implements HttpListener, Serializable {
    /**
     * 不使用水位线
     */
    public static final int WATERMARK_NONE = 0;
    /**
     * 1秒钟为周期的水位线
     */
    public static final int WATERMARK_1_SECOND = 1000;
    /**
     * 100毫秒为周期的水位线
     */
    public static final int WATERMARK_100_MILLISECOND = 100;
    /**
     * flink流
     */
    private SourceFunction.SourceContext<HttpDto<String>> ctx;
    /**
     * flink数据集
     */
    private LinkedBlockingQueue<HttpDto<String>> queue;
    /**
     * 响应监听器
     */
    private Map<String, HttpResponseListener<?>> listenerMap;
    /**
     * 端口
     */
    private int port;
    /**
     * 是否开启水位线
     */
    private int watermark;
    /**
     * 最后一个水位线的时间
     */
    private long lastWatermark;
    /**
     * 水位线线程池，只有开启水位线时，才会创建
     */
    private ExecutorService watermarkExecutor;
    /**
     * 单例对象
     */
    private static HttpHandler httpHandler;

    private HttpHandler(int port, int watermark) {
        this.port = port;
        this.watermark = watermark;
        this.listenerMap = new HashMap<>();
        this.lastWatermark = 0L;
        if (watermark > 0) {
            watermarkExecutor = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 根据端口获取不使用水印的http辅助类
     * 每个端口的辅助类都是唯一的
     *
     * @param port 端口
     * @return http辅助类
     */
    public static HttpHandler getHttpHandler(int port) {
        return getHttpHandler(port, WATERMARK_NONE);
    }

    /**
     * 根据端口获取指定水印的http辅助类
     * 每个端口的辅助类都是唯一的
     *
     * @param port 端口
     * @return http辅助类
     */
    public static HttpHandler getHttpHandler(int port, int watermark) {
        if (httpHandler != null) {
            return httpHandler;
        }
        synchronized (HttpHandler.class) {
            if (httpHandler != null) {
                return httpHandler;
            }
            httpHandler = new HttpHandler(port, watermark);
            return httpHandler;
        }
    }

    @Override
    public void putHttpResponseListener(String key, HttpResponseListener<?> listener) {
        listenerMap.put(key, listener);
    }

    @Override
    public HttpResponseListener<?> getHttpResponseListener(String key) {
        return listenerMap.get(key);
    }

    @Override
    public void removeHttpResponseListener(String key) {
        listenerMap.remove(key);
    }

    @Override
    public void action(FullHttpRequest request, String connectId, long timestamp) {
        if (ctx != null) {
            //向后传递body中的信息
            ctx.collectWithTimestamp(createData(request, connectId), timestamp);
        }
        if (queue != null) {
            try {
                queue.put(createData(request, connectId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //发送完数据后，发送水位线
        emitWatermark(timestamp);
    }

    /**
     * 发送水位线
     * 异步发送水位线，不阻塞netty线程处理数据
     *
     * @param timestamp 当前时间戳
     */
    public void emitWatermark(long timestamp) {
        if (watermark == 0) {
            return;
        }
        long currentWatermark = timestamp / watermark * watermark + watermark;
        //如果已经发送过水位线，则不发送了
        if (lastWatermark >= currentWatermark) {
            return;
        }
        lastWatermark = currentWatermark;
        watermarkExecutor.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(currentWatermark - timestamp);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("水位线线程等待异常", e);
            }
            if (ctx != null) {
                //发送水位线
                ctx.emitWatermark(new Watermark(currentWatermark));
            }
        });
    }

    /**
     * 创建数据
     *
     * @param request   本次的请求对象
     * @param connectId 连接的唯一标识
     * @return
     */
    private StringHttpDto createData(FullHttpRequest request, String connectId) {
        HttpHeaders headers = request.headers();
        String content = request.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
        StringHttpDto httpDto = new StringHttpDto(content);
        httpDto.setConnectId(connectId);
        httpDto.setHead(StatefulDto.LICENSE_KEY, headers.get(StatefulDto.LICENSE_KEY));
        //其他header信息再说
        httpDto.setMethod(request.method());
        httpDto.setUri(request.uri());
        httpDto.setPort(port);
        return httpDto;
    }
}
