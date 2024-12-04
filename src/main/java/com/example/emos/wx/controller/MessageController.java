package com.example.emos.wx.controller;

import com.example.emos.wx.Task.MessageTask;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.DeleteMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByPageForm;
import com.example.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.example.emos.wx.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/message")
@Api("消息模块网络接口")
public class MessageController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader String token) {
        int userId = jwtUtil.getUserId(token);
        Integer page = form.getPage();
        Integer length = form.getLength();
        int start = (page - 1) * length;
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form) {
        String id = form.getId();
        HashMap map = messageService.searchMessageById(id);
        return R.ok().put("result", map);
    }

    @PostMapping("/UpdateUnreadMessage")
    @ApiOperation("更新未读数据为已读消息")
    public R UpdateUnreadMessage(@RequestBody UpdateUnreadMessageForm form) {
        String id = form.getId();
        long rows = messageService.updateUnreadMessage(id);
        return R.ok().put("result", rows==1?true:false);
    }

    @PostMapping("/deleteMessageById")
    @ApiOperation("根据id删除消息")
    public R deleteMessageById(@RequestBody DeleteMessageByIdForm form) {
        String id = form.getId();
        long rows = messageService.deleteMessageRefById(id);
        return R.ok().put("result", rows==1?true:false);
    }

    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        messageTask.receiveAsync(userId + "");
        long lastCount = messageService.searchLastCount(userId);
        long unreadCount = messageService.searchUnreadCount(userId);
        return R.ok().put("lastRows", lastCount).put("unreadRows", unreadCount);
    }
}
