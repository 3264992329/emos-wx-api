package com.example.emos.wx;

import cn.hutool.core.util.IdUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/test")
@SpringBootTest
@Api("测试Web接口")
public class TestController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(){
        return R.ok().put("message","HelloWorld");
    }

    @Test
    void contextLoads() {
        for (int i = 101; i <= 150; i++) {
            MessageEntity message = new MessageEntity();
            message.setUuid(IdUtil.simpleUUID());
            message.setSenderId(0);
            message.setSenderName("系统消息");
            message.setMsg("这是第" + i + "条测试消息");
            message.setSendTime(new Date());
            String id=messageService.insertMessage(message);

            MessageRefEntity ref=new MessageRefEntity();
            ref.setMessageId(id);
            ref.setReceiverId(19); //注意：这是接收人ID
            ref.setLastFlag(true);
            ref.setReadFlag(false);
            messageService.insertRef(ref);
        }
    }

}
