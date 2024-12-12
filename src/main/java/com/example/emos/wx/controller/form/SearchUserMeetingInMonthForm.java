package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class SearchUserMeetingInMonthForm {
    @Range(min = 2020, max = 9999)
    private String year;

    @Range(min = 1, max = 12)
    private String month;
}
