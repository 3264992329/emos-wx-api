package com.example.emos.wx.config;

import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类，用于捕获控制器中未处理的异常并返回统一的错误信息。
 */
@Slf4j
@RestControllerAdvice // 声明为全局异常处理类
public class ExceptionAdvice {

    /**
     * 处理所有异常的统一方法。
     *
     * @param e 捕获的异常对象
     * @return 错误信息，返回给前端
     */
    @ResponseBody // 将返回结果序列化为 JSON 格式
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置 HTTP 状态码为 500（内部服务器错误）
    @ExceptionHandler(Exception.class) // 捕获所有类型为 Exception 的异常
    public String validException(Exception e) {
        // 将异常信息记录到日志中
        log.error("执行错误",e);

        // 如果异常是方法参数校验失败的异常（如 @Valid 或 @Validated 校验失败）
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            // 获取校验失败的错误信息（仅取第一个错误信息）
            return exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        // 如果异常是自定义的 EmosException
        else if (e instanceof EmosException) {
            EmosException exception = (EmosException) e;
            // 返回自定义异常的错误信息
            return exception.getMessage();
        }
        // 如果异常是权限不足异常（Shiro 框架）
        else if (e instanceof UnauthorizedException) {
            // 返回固定的错误信息，提示用户无权限
            return "你没有权限";
        }
        else {
            // 返回通用错误信息
            return "后端处理异常";
        }
    }
}
