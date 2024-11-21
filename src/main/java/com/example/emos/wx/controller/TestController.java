package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试Web接口")
public class TestController {

    @ApiOperation("最简单的测试方法")
    @PostMapping("/sayHello")
    //@Valid：用于启用校验机制
    public R sayHello(@RequestBody @Valid TestSayHelloForm form) {
        return R.ok().put("message","Hello"+form.getName()+"!");
    }
}
