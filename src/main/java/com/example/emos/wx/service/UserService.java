package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;

import java.util.Set;

public interface UserService {
    public int registerUser(String registerCode,String code,String nickname,String photo);

    /*
    * 查询权限
    * */
    public Set<String> searchUserPermissions(int userId);

    public Integer login(String Code);

    public TbUser selectById(int UserId);
}
