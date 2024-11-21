package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ApiModel
public class TestSayHelloForm {
//    @NotBlank  //非空字符串
//    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$") //2到15个字符
    @ApiModelProperty("姓名")
    private String name;
}
