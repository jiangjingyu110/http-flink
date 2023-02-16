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
package org.jjy.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.jjy.netty.dto.HttpDto;
import org.jjy.netty.function.HttpSource;

/**
 * 第一个使用程序
 *
 * @author 姜静宇 2023年2月16日
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        //创建运行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<HttpDto<String>> source = env.addSource(new HttpSource(8080));
        source.print("hello");
        env.execute("HelloWorld");

    }
}
