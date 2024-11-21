package com.example.emos.wx.config.shiro;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;
    @Autowired
    private ThreadLocalToken threadLocalToken;
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    * 拦截请求后，把令牌字符串封装成令牌对象
    * */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestToken = getRequestToken(req);
        if (StringUtils.isBlank(requestToken)){
            return null;
        }
        return new OAuth2Token(requestToken);
    }

    /*
    * 拦截请求，判断是否需要被shiro处理
    * */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        // Ajax提交application/json数据的时候，会先发出options请求
        // 这里要放行options请求，不需要shiro处理
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }

    /*
    * 处理所有应该被Shiro处理的请求
    * */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Content-type","text/html;charset=UTF-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Origin", resp.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        //获取请求token,如果请求里没有token,直接返回401
        String token = getRequestToken(req);
        try {
            jwtUtil.verifyToken(token); //检查令牌是否过期
        }catch (TokenExpiredException e){ //如果令牌过期，检查Redis中是否有令牌，如果存在，就重新生产一个令牌给客户端
            if (redisTemplate.hasKey("token")){ //Redis中有令牌，更新令牌
                redisTemplate.delete(token);//删除Redis令牌
                int userId = jwtUtil.getUserId(token);
                String newToken = jwtUtil.createToken(userId);//生成新的Redis令牌
                redisTemplate.opsForValue().set("token", newToken);//将新令牌保存到Redis中
                threadLocalToken.setToken(newToken);//将新令牌保存到Threadlocal中
            }else {//Redis中没有令牌
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }
        }catch (JWTDecodeException e){//如果Redis不存在令牌，让用户重新登录
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print("无效令牌");
            return false;
        }
        boolean bool = executeLogin(request, response);
        return bool;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setHeader("Content-type","text/html;charset=UTF-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Origin", resp.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            resp.getWriter().print(e.getMessage());
        } catch (IOException ex) {

        }
        return false;
    }

    /*
    * 获取请求头里的token
    * */
    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)){
            token = request.getParameter("token");
        }
        return token;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }
}
