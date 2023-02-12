package org.jjy.netty.dto;

/**
 * string对象传输信息
 * HttpSink和HttpSource都使用这个对象传输
 *
 * @author 姜静宇 2023年2月12日
 */
public class StringHttpDto extends DefaultHttpDto<String> {
    public StringHttpDto(String data) {
        super(data);
    }
}
