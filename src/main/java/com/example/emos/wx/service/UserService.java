package com.example.emos.wx.service;

import java.util.Set;

public interface UserService {
    public int registerUser(String registerCode,String code,String nickname,String photo);

    /*
    * 查询权限
    * */
    public Set<String> searchUserPermissions(int userId);
}
