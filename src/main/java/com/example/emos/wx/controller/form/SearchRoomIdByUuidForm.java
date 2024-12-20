package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SearchRoomIdByUuidForm {
    @NotBlank
    private String uuid;
}
