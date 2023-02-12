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

/**
 * 待状态的dto
 *
 * @author 姜静宇 2023年2月12日
 */
public interface StatefulDto {
    /**
     * 许可码在请求头信息中的key
     */
    String LICENSE_KEY = "token";

    /**
     * 获取许可
     *
     * @return 许可
     */
    String getLicense();
}
