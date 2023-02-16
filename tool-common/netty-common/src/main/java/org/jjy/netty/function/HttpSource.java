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
package org.jjy.netty.function;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.jjy.netty.dto.HttpDto;
import org.jjy.netty.handler.HttpHandler;
import org.jjy.netty.handler.WebSocketHandler;
import org.jjy.netty.server.HttpServer;

/**
 * http数据源
 * 流式获取http数据
 * <p>
 * 所有的请求都会被source捕获，后面可以从source拿到数据后，再进行分流等处理
 *
 * @author 姜静宇 2023年2月16日
 */
public class HttpSource extends RichSourceFunction<HttpDto<String>> {
    /**
     * http服务
     */
    private HttpServer httpServer;
    /**
     * http处理
     */
    private HttpHandler httpHandler;
    /**
     * webSocket处理
     */
    private WebSocketHandler webSocketHandler;
    /**
     * 水位线时间间隔
     */
    private final int watermark;
    /**
     * 端口
     */
    private final int port;

    /**
     * 使用指定端口和水位线构造，有聚合或连接需要的，可以使用这个
     *
     * @param port      端口
     * @param watermark 水位线时间间隔
     */
    public HttpSource(int port, int watermark) {
        this.port = port;
        this.watermark = watermark;
    }

    /**
     * 使用指定端口构造，无聚合或连接需求的，可以使用这个
     *
     * @param port 端口
     */
    public HttpSource(int port) {
        this(port, HttpHandler.WATERMARK_NONE);
    }

    @Override
    public void run(SourceContext<HttpDto<String>> ctx) throws Exception {
        this.httpHandler.setCtx(ctx);
        this.webSocketHandler.setCtx(ctx);
        //这个方法自己会卡住，不需要running
        httpServer.run();
    }

    @Override
    public void cancel() {
        httpServer.close();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        httpHandler = HttpHandler.getHttpHandler(port, watermark);
        webSocketHandler = WebSocketHandler.getWebSocketHandler();
        httpServer = new HttpServer(httpHandler.getPort(), httpHandler, webSocketHandler);
    }

    @Override
    public void close() throws Exception {
        httpServer.close();
    }
}
