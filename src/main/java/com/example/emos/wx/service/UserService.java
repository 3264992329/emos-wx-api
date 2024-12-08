package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface UserService {
    public int registerUser(String registerCode,String code,String nickname,String photo);

    /*
    * 查询权限
    * */
    public Set<String> searchUserPermissions(int userId);

    public Integer login(String Code);

    public TbUser selectById(int UserId);

    //查询用户入职日期
    public String searchUserHiredate(int UserId);

    //用户页面数据加载
    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);
}
