package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbCheckinDao {
    public void insertCheckin(TbCheckin param);
    public Integer haveCheckin(HashMap param);
    //查询员工当天签到情况
    public HashMap searchTodayCheckin(int userId);
    //员工请考勤日期总数
    public long searchCheckinDays(int userId);
    //
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
}