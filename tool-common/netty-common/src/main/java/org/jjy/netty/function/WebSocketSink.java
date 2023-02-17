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

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.jjy.netty.dto.HttpDto;
import org.jjy.netty.handler.WebSocketHandler;
import org.jjy.netty.listener.WebSocketResponseListener;

/**
 * webSocket输出
 *
 * @author 姜静宇 2023年2月17日
 */
public class WebSocketSink implements SinkFunction<HttpDto<String>> {
    @Override
    public void invoke(HttpDto<String> value, Context context) throws Exception {
        WebSocketHandler webSocketHandler = WebSocketHandler.getWebSocketHandler();
        String connectId = value.getConnectId();
        WebSocketResponseListener listener = webSocketHandler.getWebSocketResponseListener(connectId);
        if (listener != null) {
            try {
                listener.send(value.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
