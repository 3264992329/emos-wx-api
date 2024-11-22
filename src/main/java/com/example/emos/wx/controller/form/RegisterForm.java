package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ApiModel
@Data
public class RegisterForm {

    @Pattern(regexp = "^[0-9]{6}$",message = "注册码必须是6位数字")
    @NotBlank(message = "注册码不能为空")
    private String registerCode;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "微信临时授权不能为空")
    private String code;

    @NotBlank(message = "头像不能为空")
    private String photo;

}