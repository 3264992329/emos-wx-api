package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbDeptDao {
    public ArrayList<HashMap> searchDeptMembers(String keyword);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);


}