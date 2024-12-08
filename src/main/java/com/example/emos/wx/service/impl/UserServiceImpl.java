package com.example.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.Task.MessageTask;
import com.example.emos.wx.db.dao.TbDeptDao;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {
    @Value("${wx.app-id}")
    private String appId;
    @Value("${wx.app-secret}")
    private String appSecret;
    @Autowired
    private TbUserDao userDao;
    @Autowired
    private MessageTask messageTask;
    @Autowired
    private TbDeptDao deptDao;

    private String getOpenId(String code){
        String url="https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String respose = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(respose);
        String openId = json.getStr("openid");
        if(openId == null || openId.length() == 0){
            throw new RuntimeException("临时登录凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        //邀请码为“000000”代表是超级管理员注册
        if (registerCode.equals("000000")){
            //查询超级管理员是否已存在
            boolean bool = userDao.haveRootUser();
            if(!bool){
                String openId = getOpenId(code);
                HashMap map = new HashMap();
                map.put("openId", openId);
                map.put("nickname", nickname);
                map.put("photo", photo);
                map.put("role","[0]");
                map.put("status",1);
                map.put("create_time", new Date());
                map.put("root",true);
                userDao.insert(map);
                Integer id = userDao.selectByOpenId(openId);

                //异步发送系统管理员注册成功消息
                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("你已成功注册为超级管理员，请及时更新你的个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id+"",entity);

                return id;
            }else {
                throw new EmosException("无法绑定超级管理员账号");
            }
        }else {
            return 0;
        }

    }



    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public Integer login(String Code) {
        String openId = getOpenId(Code);
        Integer id = userDao.selectByOpenId(openId);
        if (id == null){
            throw new EmosException("账户不存在");
        }

        // 获取消息，放到消息表中
        //messageTask.receiveAsync(id+"");
        return id;
    }

    @Override
    public TbUser selectById(int UserId) {
        TbUser tbUser = userDao.selectById(UserId);
        return tbUser;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list1 = deptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list2 = deptDao.searchUserGroupByDept(keyword);
        for (HashMap map1 : list1) {
            String id1 = map1.get("id").toString();
            ArrayList members = new ArrayList();
            for (HashMap map2 : list2) {
                String id2 = map2.get("deptId").toString();
                if (id2.equals(id1)){
                    members.add(map2);
                }
            }
            map1.put("members", members);
        }
        return list1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userDao.searchMembers(param);
        return list;
    }

}
