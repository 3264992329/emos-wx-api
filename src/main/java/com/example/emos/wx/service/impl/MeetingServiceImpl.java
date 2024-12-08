package com.example.emos.wx.service.impl;

import com.example.emos.wx.db.dao.TbMeetingDao;
import com.example.emos.wx.db.pojo.TbMeeting;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private TbMeetingDao meetingDao;

    @Override
    public void insertMeeting(TbMeeting entity) {
        int i = meetingDao.insertMeeting(entity);
        if (i != 1){
            throw new EmosException("会议添加失败");
        }
        //TODO 开启审批工作流
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

}
