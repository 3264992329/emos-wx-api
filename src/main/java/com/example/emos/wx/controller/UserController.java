package com.example.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.*;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.service.impl.UserServiceImpl;
import com.tencentyun.TLSSigAPIv2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @Value("${trtc.appid}")
    private Integer appid;
    @Value("${trtc.key}")
    private String key;
    @Value("${trtc.expire}")
    private Integer expire;

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

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，并安装部门分组排序")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        String keyword = form.getKeyword();
        ArrayList<HashMap> list = userService.searchUserGroupByDept(keyword);
        return R.ok().put("result", list);
    }

    @ApiOperation("查询成员")
    @PostMapping("/searchMembers")
    @RequiresPermissions(value = {"root","meeting:insert","meeting:update"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        String members = form.getMembers();
        if (!JSONUtil.isJsonArray(members)){
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(members).toList(Integer.class);
        ArrayList<HashMap> list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }

    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户头像和姓名")
    public R selectUserPhotoAndName(@RequestBody @Valid SelectUserPhotoAndNameForm form){
        if (!JSONUtil.isJsonArray(form.getIds())){
            throw new EmosException("list不是json格式的数组");
        }
        List<Integer> param = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> list = userService.selectUserPhotoAndName(param);
        return R.ok().put("result", list);
    }

    @PostMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R searchMemberEmail(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String email = userService.searchMemberEmail(userId);

        TLSSigAPIv2 api = new TLSSigAPIv2(appid, key);
        String userSig = api.genUserSig(email, expire);
        return R.ok().put("userSig", userSig).put("email", email);
    }
}
