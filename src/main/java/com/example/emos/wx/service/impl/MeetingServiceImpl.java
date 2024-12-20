package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.db.dao.TbMeetingDao;
import com.example.emos.wx.db.dao.TbUserDao;
import com.example.emos.wx.db.pojo.TbMeeting;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.code}")
    private String code;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void insertMeeting(TbMeeting entity) {
        int i = meetingDao.insertMeeting(entity);
        if (i != 1){
            throw new EmosException("会议添加失败");
        }
        // 开启审批工作流
        startMeetingWorkflow(entity.getUuid(),entity.getCreatorId().intValue(),entity.getDate(),entity.getStart());
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);
        String date = null;
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null;
        cn.hutool.json.JSONArray array = null;
        for (HashMap map : list) {
            String temp = map.get("date").toString();
            if (!temp.equals(date)) {
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date", date);
                array = new cn.hutool.json.JSONArray();
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            array.put(map);
        }
        return resultList;
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = meetingDao.searchMeetingById(id);
        ArrayList<HashMap> list = meetingDao.searchMeetingMembers(id);
        map.put("members", list);
        return map;
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int)param.get("id");
        String start = param.get("start").toString();
        String end = param.get("end").toString();
        String instanceId = param.get("instanceId").toString();

        HashMap oldMeeting = meetingDao.searchMeetingById(id);
        String uuid = oldMeeting.get("uuid").toString();
        Integer creatorId = Integer.parseInt(oldMeeting.get("creatorId").toString());

        //更新会议记录
        int row = meetingDao.updateMeetingInfo(param);
        if (row != 1){
            throw new EmosException("会议更新失败");
        }

        //更新成功后，删除之前的工作流
        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("reason","会议被修改");
        json.set("code",code);
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (resp.getStatus()!=200){
            log.error("删除工作流失败");
            throw new EmosException("删除工作流失败");
        }

        //开启新的工作流
        startMeetingWorkflow(uuid,creatorId,start,end);
    }

    @Override
    public void deleteMeetingById(int id) {
        HashMap meeting = meetingDao.searchMeetingById(id);
        String uuid = meeting.get("uuid").toString();
        DateTime date = DateUtil.parse(meeting.get("date") + " " + meeting.get("start"));
        DateTime now = DateUtil.date();
        String instanceId = meeting.get("instanceId").toString();

        if (now.isAfterOrEquals(date.offset(DateField.MINUTE,-20))){
            throw new EmosException("不能在会议开始前20分钟取消");
        }
        int row = meetingDao.deleteMeetingById(id);
        if (row != 1){
            throw new EmosException("删除会议失败");
        }

        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("reason","会议被删除");
        json.set("code",code);
        String url = workflow + "/workflow/deleteProcessById";

        HttpResponse response = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
        if (response.getStatus()!=200){
            log.error("删除工作流失败");
            throw new EmosException("删除工作流失败");
        }

    }

    @Override
    public Long searchRoomIdByUuid(String uuid) {

        Object temp = redisTemplate.opsForValue().get(uuid);
        long roomId = Long.parseLong(temp.toString());
        return roomId;
    }

    @Override
    public List<String> searchUserMeetingInMonth(HashMap param) {
        List<String> list = meetingDao.searchUserMeetingInMonth(param);
        return list;
    }

    private void startMeetingWorkflow(String uuid, int creatorId, String date, String start) {
        HashMap info = userDao.searchUserInfo(creatorId); //查询创建者用户信息

        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code",code);
        json.set("date",date);
        json.set("start",start);
        String[] roles = info.get("roles").toString().split("，");
        //如果不是总经理创建的会议
        if (!ArrayUtil.contains(roles, "总经理")) {
            //查询总经理ID和同部门的经理的ID
            Integer managerId = userDao.searchDeptManagerId(creatorId);
            json.set("managerId", managerId); //部门经理ID
            Integer gmId = userDao.searchGmId();//总经理ID
            json.set("gmId", gmId);
            //查询会议员工是不是同一个部门
            boolean bool = meetingDao.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }
        String url = workflow+"/workflow/startMeetingProcess";
        //请求工作流接口，开启工作流
        HttpResponse response = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
        if (response.getStatus() == 200) {
            json = JSONUtil.parseObj(response.body());
            //如果工作流创建成功，就更新会议状态
            String instanceId = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);
            int row = meetingDao.updateMeetingInstanceId(param); //在会议记录中保存工作流实例的ID
            if (row != 1) {
                throw new EmosException("保存会议工作流实例ID失败");
            }
        }
    }


}
