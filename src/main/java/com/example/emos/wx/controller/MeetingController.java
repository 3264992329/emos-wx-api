package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.*;
import com.example.emos.wx.db.pojo.TbMeeting;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    @ApiOperation("添加会议")
    @PostMapping("/insertMeeting")
    @RequiresPermissions(value = {"ROOT","MEETING:INSERT"},logical = Logical.OR)
    public R insertMeeting(@Valid @RequestBody InsertMeetingForm form, @RequestHeader("token") String token) {
        //如果为线下会议，判断当前会议地点是否为空
        if (form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmosException("线下会议地点不能为空");
        }
        //判断结束时间是否在开始时间之后
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.before(d1)){
            throw new EmosException("开始时间不能在结束时间之后");
        }
        //判断members是不是json数组
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        TbMeeting entity = new TbMeeting();
        entity.setUuid(UUID.randomUUID().toString());
        entity.setTitle(form.getTitle());
        entity.setCreatorId((long)jwtUtil.getUserId(token));
        entity.setDate(form.getDate());
        entity.setPlace(form.getPlace());
        entity.setStart(form.getStart()+":00");
        entity.setEnd(form.getEnd()+":00");
        entity.setType((short) form.getType());
        entity.setMembers(form.getMembers());
        entity.setDesc(form.getDesc());
        entity.setStatus((short) 1);

        meetingService.insertMeeting(entity);
        return R.ok().put("result", "success");
    }

    @PostMapping("/searchMeetingById")
    @ApiOperation("根据会议id查询会议")
    @RequiresPermissions(value = {"ROOT","MEETING:SELECT"},logical = Logical.OR)
    public R searchMeetingById(@RequestBody @Valid SearchMeetingByIdForm form) {
        HashMap map = meetingService.searchMeetingById(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议信息")
    @RequiresPermissions(value = {"ROOT","MEETING:UPDATE"},logical = Logical.OR)
    public R updateMeetingInfo(@RequestBody @Valid UpdateMeetingInfoForm form){
        //如果为线下会议，判断当前会议地点是否为空
        if (form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmosException("线下会议地点不能为空");
        }
        //判断结束时间是否在开始时间之后
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.before(d1)){
            throw new EmosException("开始时间不能在结束时间之后");
        }
        //判断members是不是json数组
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        HashMap map = new HashMap();
        map.put("id", form.getId());
        map.put("title", form.getTitle());
        map.put("date", form.getDate());
        map.put("place", form.getPlace());
        map.put("start", form.getStart()+":00");
        map.put("end", form.getEnd()+":00");
        map.put("type", form.getType());
        map.put("members", form.getMembers());
        map.put("desc", form.getDesc());
        map.put("instanceId",form.getInstanceId());
        map.put("status", 1);
        meetingService.updateMeetingInfo(map);
        return R.ok().put("result", "success");
    }

    @PostMapping("/deleteMeetingById")
    @ApiOperation("根据ID删除会议")
    @RequiresPermissions(value = {"ROOT","MEETING:DELETE"},logical = Logical.OR)
    public R deleteMeetingById(@RequestBody @Valid DeleteMeetingByIdForm form) {
        meetingService.deleteMeetingById(form.getId());
        return R.ok().put("result", "success");
    }

    @PostMapping("/recieveNotify")
    @ApiOperation("接收工作流通知")
    public R recieveNotify(@RequestBody @Valid RecieveNotifyForm form) {
        if (form.getResult().equals("同意")){
            log.debug(form.getUuid()+"的会议审批通过");
        }else {
            log.debug(form.getUuid()+"审批通过");
        }
        return R.ok();
    }

    @PostMapping("/searchRoomIdByUuid")
    @ApiOperation("查询会议房间RoomID")
    public R searchRoomIdByUuid(@RequestBody @Valid SearchRoomIdByUuidForm form) {
        Long roomId = meetingService.searchRoomIdByUuid(form.getUuid());
        return R.ok().put("result", roomId);
    }

    @PostMapping("/searchUserMeetingInMonth")
    @ApiOperation("查询用户当月会议日期列表")
    public R searchUserMeetingInMonth(@RequestBody @Valid SearchUserMeetingInMonthForm form,@RequestHeader("token") String token) {
        String month = form.getMonth();
        String year = form.getYear();
        HashMap param = new HashMap();
        int userId = jwtUtil.getUserId(token);
        param.put("userId", userId);
        param.put("express", month+"/"+year);
        List<String> list = meetingService.searchUserMeetingInMonth(param);
        return R.ok().put("result", list);
    }
}
