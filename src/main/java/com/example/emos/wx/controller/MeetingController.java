package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.SearchMyMeetingListByPageForm;
import com.example.emos.wx.service.MeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/meeting")
@Slf4j
@Api("会议模块网络接口")
public class MeetingController {
    @Autowired
    private MeetingService meetingService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("分页查询会议列表信息")
    @PostMapping("/searchMyMeetingListByPage")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token") String token) {
        int page = form.getPage();
        int length = form.getLength();
        int userId = jwtUtil.getUserId(token);
        long start = (page - 1) * length;
        HashMap map = new HashMap();
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        ArrayList list = meetingService.searchMyMeetingListByPage(map);
        log.info("会议信息list:{}", list);
        return R.ok().put("result", list);
    }
}
