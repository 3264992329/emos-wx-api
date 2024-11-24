package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.SysConfigDao;
import com.example.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync
public class EmosWxApiApplication {
    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants systemConstants;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init(){
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(one -> {
            String paramKey = one.getParamKey();
            paramKey = StrUtil.toCamelCase(paramKey); //从下划线的形式，到驼峰命名的方式
            String paramValue = one.getParamValue();
            try {
                /*Field 类
                  表示类的字段。
                  提供方法访问字段的值（get）和设置字段的值（set）。*/
                Field field = systemConstants.getClass().getDeclaredField(paramKey);
                field.set(systemConstants,paramValue);
            } catch (Exception e) {
                log.error("执行异常:"+e.getMessage(),e);
            }
        });
    }

}
