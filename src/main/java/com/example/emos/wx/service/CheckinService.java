package com.example.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {
    public String validCanCheckIn(int UserId, String data);
    public void checkIn(HashMap param);
    public void createFaceModel(int UserId, String path);
    //查询员工当天签到情况
    public HashMap searchTodayCheckin(int userId);
    //员工请考勤日期总数
    public long searchCheckinDays(int userId);
    //
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
}
