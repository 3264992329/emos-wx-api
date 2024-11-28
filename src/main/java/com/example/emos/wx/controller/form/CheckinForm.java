package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.stereotype.Component;

@ApiModel
@Data
public class CheckinForm {
    private String address;
    private String country;
    private String province;
    private String city;
    private String district;
}
