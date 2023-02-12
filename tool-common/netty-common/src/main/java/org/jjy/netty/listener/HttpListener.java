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

/**
 * http监听器
 *
 * @author 姜静宇 2023年2月12日
 */
public interface HttpListener extends HttpRequestListener {
    /**
     * 添加相应监听器
     *
     * @param key      监听器的key，一般使用连接id
     * @param listener 响应监听器
     */
    void putHttpResponseListener(String key, HttpResponseListener<?> listener);

    /**
     * 通过关键字获取相应监听器
     *
     * @param key 监听器的key，一般使用连接id
     * @return 响应监听器
     */
    HttpResponseListener<?> getHttpResponseListener(String key);

    /**
     * 移除相应监听器
     *
     * @param key 监听器的key，一般使用连接id
     */
    void removeHttpResponseListener(String key);
}
