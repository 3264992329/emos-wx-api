package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbHolidaysDao {
    public Integer searchTodayIsHolidays();
    //查询特定范围内的特殊节假日
    public ArrayList<String> searchHolidaysInRange(HashMap param);
}