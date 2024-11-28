package com.example.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.service.CheckinService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping("/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户是否可以签到")
    private R validCanCheckIn(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

    @ApiOperation("人脸签到")
    @PostMapping("/checkin")
    private R checkIn(@Valid CheckinForm form,@RequestParam("photo") MultipartFile file,@RequestHeader("token") String token){
        if (file == null) {
            return R.error("照片为空");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + fileName;
        //判断是否是jpg图片
        if (!fileName.endsWith(".jpg")){
            FileUtil.del(path); //删除文件
            return R.error("必须要是以JPG结尾的图片");
        }else {

            try {
                file.transferTo(Paths.get(path));
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("fileName", fileName);
                map.put("path", path);
                map.put("address",form.getAddress());
                map.put("province",form.getProvince());
                map.put("city",form.getCity());
                map.put("district",form.getDistrict());
                checkinService.checkIn(map);
                return R.ok("签到成功");
            }catch (IOException e){
                log.error(e.getMessage());
                throw new RuntimeException("保存图片错误");
            }finally {
                FileUtil.del(path);
            }

        }

    }

}
