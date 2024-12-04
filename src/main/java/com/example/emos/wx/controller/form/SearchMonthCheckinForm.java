package com.example.emos.wx.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Data
public class SearchMonthCheckinForm {
    @NotNull
    @Range(min = 1, max = 12)
    private Integer month;

    @NotNull
    @Range(min = 2000, max = 3000)
    private Integer year;
}
