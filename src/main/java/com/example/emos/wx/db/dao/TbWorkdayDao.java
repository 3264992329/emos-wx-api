package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbWorkdayDao {
    public Integer searchTodayIsWorkdays();
    //查询范围内特殊工作日
    public ArrayList<String> searchWorkdayInRange(HashMap param);
}