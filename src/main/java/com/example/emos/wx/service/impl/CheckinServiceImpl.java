package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.SysConfigDao;
import com.example.emos.wx.db.dao.TbCheckinDao;
import com.example.emos.wx.db.dao.TbHolidaysDao;
import com.example.emos.wx.db.dao.TbWorkdayDao;
import com.example.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {
    @Autowired
    private TbHolidaysDao tbHolidaysDao;

    @Autowired
    private TbWorkdayDao tbWorkdayDao;

    @Autowired
    private TbCheckinDao tbCheckinDao;
    @Autowired
    private SystemConstants systemConstants;

    @Override
    public String validCanCheckIn(int UserId, String date) {
        boolean bool_1 = tbHolidaysDao.searchTodayIsHolidays() != null ? true : false;
        boolean bool_2 = tbWorkdayDao.searchTodayIsWorkdays() != null ? true : false;
        String type="工作日";
        //判断当前是否为周末
        if (DateUtil.date().isWeekend()){
            type="节假日";
        }
        //判断当前是否为特殊节假日
        if (bool_1){
            type="节假日";
        }
        //判断当前是否为特殊工作日
        if (bool_2){
            type="工作日";
        }
        //判断当前时间是否可以签到
        if (type=="节假日"){
            return "节假日不需考勤";
        }else {
            DateTime now = DateUtil.date();
            String begin = DateUtil.today()+" "+systemConstants.getAttendanceStartTime();
            String end = DateUtil.today()+" "+systemConstants.getAttendanceEndTime();
            DateTime beginTime = DateUtil.parse(begin);
            DateTime endTime = DateUtil.parse(end);
            //如果当前时间小于考勤开始时间
            if (now.isBefore(beginTime)){
                return "还没到考勤时间，不能签到";
            }
            //如果当前时间大于考勤结束时间
            else if (now.isAfter(endTime)){
                return "考勤时间已过，不能签到";
            }else {
                HashMap map = new HashMap();
                map.put("userId", UserId);
                map.put("data", date);
                map.put("beginTime", beginTime);
                map.put("endTime", endTime);
                boolean b = tbCheckinDao.haveCheckin(map) != null ? true : false;
                return b ? "已经签到，不能重复签到":"可以签到";
            }
        }
    }

}
