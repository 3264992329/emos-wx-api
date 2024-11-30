package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.db.dao.*;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    @Autowired
    private TbFaceModelDao tbFaceModelDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.code}")
    private String code;

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

    @Override
    public void checkIn(HashMap param) {
        Date d1 = DateUtil.date();//当前时间
        //签到开始时间
        Date d2 = DateUtil.parse(DateUtil.today()+" "+systemConstants.getAttendanceStartTime());
        //签到结束时间
        Date d3 = DateUtil.parse(DateUtil.today()+" "+systemConstants.getAttendanceEndTime());
        //签到时间判断
        int status=1;
        if (d1.compareTo(d2)<=0){
            status=1;  //正常签到
        }else if (d1.compareTo(d3)<0 && d1.compareTo(d2)>0){
            status=2;  //迟到
        }
        //查询人脸模型
        Integer userId = (Integer) param.get("userId");
        String faceModel = tbFaceModelDao.searchFaceModel(userId);
        if (faceModel == null){
            throw new EmosException("不存在人脸模型");
        }else {
            String path = (String) param.get("path");
            //发起人脸数据比对请求
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path),"targetModel",faceModel);
            request.form("code",code);
            HttpResponse response = request.execute();//发起请求
            //判断响应信息
            if ("无法识别出人脸".equals(response.body()) || "照片中存在多张人脸".equals(response.body())){
                throw new EmosException(response.body());
            }else if ("False".equals(response.body())){
                throw new EmosException("签到无效，非本人签到");
            }else if ("True".equals(response.body())){
                //TODO 查询疫情风险等级
                int risk=1;
                //TODO 保存签到记录
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                String city = (String) param.get("city");
                String districet = (String) param.get("districet");

                TbCheckin entity = new TbCheckin();
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(districet);
                entity.setStatus((byte)status);
                entity.setRisk(risk);
                entity.setDate(DateUtil.today());
                //entity.setCreateTime(DateUtil.date());
                tbCheckinDao.insertCheckin(entity);
            }
        }
    }

    @Override
    public void createFaceModel(int UserId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code",code);
        HttpResponse response = request.execute();
        String body = response.body();
        if (body.equals("无法识别出人脸") || body.equals("照片中存在多张人脸")){
            throw new EmosException(body);
        }else {
            TbFaceModel tbFaceModel = new TbFaceModel();
            tbFaceModel.setUserId(UserId);
            tbFaceModel.setFaceModel(body);
            tbFaceModelDao.insertFaceModel(tbFaceModel);
        }

    }

    //查询员工当天签到情况
    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = tbCheckinDao.searchTodayCheckin(userId);
        return map;
    }
    //员工请考勤日期总数
    @Override
    public long searchCheckinDays(int userId) {
        long days = tbCheckinDao.searchCheckinDays(userId);
        return days;
    }

    //循环查询一定范围内签到情况
    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        //查询特殊日期
        ArrayList<String> workdayList = tbWorkdayDao.searchWorkdayInRange(param);
        ArrayList<String> holidaysList = tbHolidaysDao.searchHolidaysInRange(param);
        ArrayList<HashMap> checkinList = tbCheckinDao.searchWeekCheckin(param);
        //查询范围的开始时间，结束时间
        DateTime startDate = DateUtil.parse(param.get("startDate").toString());
        DateTime endDate = DateUtil.parse(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList list = new ArrayList();

        range.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            //查询该天是否为工作日，节假日，特殊工作日或特殊节假日
            String type="工作日";
            if (one.isWeekend()){
                type="节假日";
            }
            if (holidaysList.contains(date)&&holidaysList !=null){
                type="节假日";
            }
            if (workdayList.contains(date)&&workdayList !=null){
                type="工作日";
            }

            String status="";
            //查看是签到状态，如果签到表查询不到，默认为“缺勤”
            if (type.equals("工作日")&&DateUtil.compare(one,DateUtil.date())<=0){
                status="缺勤";
                boolean flag = false;
                for (HashMap<String,String> map : checkinList) {
                    if (map.containsValue(date)){
                      status=map.get("status");
                      flag=true;
                      break;
                    }
                }

                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.getAttendanceEndTime());
                String today = DateUtil.today();
                if (date.equals(today)&&DateUtil.date().isBefore(endTime)){
                    status="";
                }

            }
            HashMap map = new HashMap();
            map.put("date", date);
            map.put("type", type);
            map.put("status", status);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        //将查询结果封装到hashmap中，并加入到list集合当中
        return list;
    }


}
