package com.example.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.service.CheckinService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户是否可以签到")
    private R validCanCheckIn(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

}
