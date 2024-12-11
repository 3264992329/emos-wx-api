package com.example.emos.wx.config.shiro;

// 导入相关的类
import org.apache.shiro.mgt.SecurityManager; // Shiro的核心安全管理器接口
import org.apache.shiro.spring.LifecycleBeanPostProcessor; // 管理Shiro生命周期的工具类
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor; // 用于注解权限验证的Advisor
import org.apache.shiro.spring.web.ShiroFilterFactoryBean; // Shiro的核心过滤器工厂
import org.apache.shiro.web.mgt.DefaultWebSecurityManager; // Shiro默认的Web安全管理器
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter; // Servlet的过滤器接口
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration // 表示这是一个配置类，Spring会自动加载
public class ShiroConfig {

    // 配置 Shiro 的核心安全管理器 SecurityManager
    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm) {
        // 使用默认的 Web 安全管理器
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置自定义的 Realm，用于身份验证和授权
        securityManager.setRealm(oAuth2Realm);
        // 禁用 RememberMe 功能
        securityManager.setRememberMeManager(null);
        return securityManager; // 返回安全管理器
    }

    // 配置 Shiro 的核心过滤器工厂，用于定义过滤规则
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, OAuth2Filter oAuth2Filter) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        // 设置安全管理器
        shiroFilter.setSecurityManager(securityManager);

        // 配置自定义的 OAuth2 过滤器
        Map<String, Filter> filters = new HashMap<>();
        filters.put("oauth2", oAuth2Filter); // 定义 oauth2 过滤器
        shiroFilter.setFilters(filters);

        // 配置过滤链，定义访问权限
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/druid/**", "anon");
        filterMap.put("/app/**", "anon");
        filterMap.put("/sys/login", "anon");
        filterMap.put("/swagger/**", "anon");
        filterMap.put("/v2/api-docs", "anon");
        filterMap.put("/swagger-ui.html", "anon");
        filterMap.put("/swagger-resources/**", "anon");
        filterMap.put("/captcha.jpg", "anon");
        filterMap.put("/user/register", "anon");
        filterMap.put("/user/login", "anon");
        filterMap.put("/meeting/recieveNotify", "anon");
        //filterMap.put("/test/**", "anon");
        filterMap.put("/**", "oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter; // 返回配置好的过滤器工厂
    }

    // 管理 Shiro 生命周期的工具类
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor(); // 返回生命周期管理器
    }

    // 配置权限注解支持
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        // 设置安全管理器，用于注解支持
        advisor.setSecurityManager(securityManager);
        return advisor; // 返回配置好的 Advisor
    }
}
