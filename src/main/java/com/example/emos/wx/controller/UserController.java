package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.LoginForm;
import com.example.emos.wx.controller.form.RegisterForm;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${emos.jwt.cache-expire}")
    private String cacheExpire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register( @RequestBody  RegisterForm form) {
        int userId = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(userId);
        Set<String> permissions = userService.searchUserPermissions(userId);
        saveCacheToken(token, userId);
        log.info("保存信息:{}，{}",token,userId);
        return R.ok("用户注册成功").put("token", token).put("permission", permissions);
    }
    /*@PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);
    }*/

    private void saveCacheToken(String token,int userId) {
        redisTemplate.opsForValue().set(token,userId+"", Long.parseLong(cacheExpire),TimeUnit.DAYS);
    }

    @PostMapping("/login")
    public R login(@RequestBody @Valid LoginForm form) {
        int id = userService.login(form.getCode());
        log.info("用户id为：{}",id);
        Set<String> permissions = userService.searchUserPermissions(id);
        String token = jwtUtil.createToken(id);
        saveCacheToken(token,id);
        return R.ok("登录成功").put("token", token).put("permission", permissions);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("用户信息页面数据查询")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }
}
