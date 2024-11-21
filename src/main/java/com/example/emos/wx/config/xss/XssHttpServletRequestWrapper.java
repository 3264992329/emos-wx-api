package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// 自定义 HttpServletRequestWrapper 用于 XSS 过滤
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    // 构造函数，接收 HttpServletRequest 对象
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 重写 getParameter 方法，过滤单个请求参数的值
     * @param name 参数名
     * @return 过滤后的参数值
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name); // 调用父类方法获取参数值
        if (value != null) {
            value = HtmlUtil.filter(value); // 使用 Hutool 工具类进行 XSS 过滤
        }
        return value;
    }

    /**
     * 重写 getParameterValues 方法，过滤数组形式的请求参数值
     * @param name 参数名
     * @return 过滤后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name); // 调用父类方法获取参数值数组
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                values[i] = HtmlUtil.filter(values[i]); // 逐一过滤参数值
            }
        }
        return values;
    }

    /**
     * 重写 getParameterMap 方法，过滤请求参数的键值对
     * @return 过滤后的参数映射
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap(); // 获取原始参数映射
        Map<String, String[]> map = new LinkedHashMap<>(); // 用 LinkedHashMap 保证顺序
        if (parameters != null) {
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                String key = entry.getKey(); // 获取参数名
                String[] values = entry.getValue(); // 获取参数值数组
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] != null && !StrUtil.hasEmpty(values[i])) {
                            // 过滤非空参数值
                            values[i] = HtmlUtil.filter(values[i]);
                        }
                    }
                }
                map.put(key, values); // 放入过滤后的映射
            }
        }
        return map;
    }

    /**
     * 重写 getHeader 方法，过滤请求头的值
     * @param name 请求头名
     * @return 过滤后的请求头值
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name); // 调用父类方法获取请求头值
        if (value != null) {
            value = HtmlUtil.filter(value); // 过滤请求头值
        }
        return value;
    }

    /**
     * 重写 getInputStream 方法，处理请求体的内容
     * @return 包含过滤后内容的 ServletInputStream
     * @throws IOException 异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 获取原始请求输入流
        ServletInputStream inputStream = super.getInputStream();

        // 读取输入流内容并存入 StringBuffer
        StringBuffer body = new StringBuffer();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        while (line != null) {
            body.append(line); // 将每行追加到 body 中
            line = bufferedReader.readLine();
        }

        // 关闭流以释放资源
        reader.close();
        bufferedReader.close();
        inputStream.close();

        // 将请求体内容解析为 Map 对象
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        Map<String, Object> resultMap = new HashMap<>(map.size());
        for (String key : map.keySet()) {
            Object val = map.get(key);
            if (val instanceof String) {
                // 对 String 类型的值进行 XSS 过滤
                resultMap.put(key, HtmlUtil.filter((String) val));
            } else {
                // 非 String 类型直接放入
                resultMap.put(key, val);
            }
        }

        // 将处理后的 Map 转回 JSON 字符串
        String jsonStr = JSONUtil.toJsonStr(resultMap);

        // 构造新的输入流以返回
        final ByteArrayInputStream bain = new ByteArrayInputStream(jsonStr.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bain.read(); // 从字节数组流中读取数据
            }

            @Override
            public boolean isFinished() {
                return false; // 表示未完成（可以根据实际需要调整逻辑）
            }

            @Override
            public boolean isReady() {
                return false; // 表示未准备好（可以根据实际需要调整逻辑）
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 空实现
            }
        };
    }
}
