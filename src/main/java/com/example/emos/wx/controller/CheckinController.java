package com.example.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.exception.EmosException;
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

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    private R createFaceModel(@RequestParam("photo") MultipartFile file,@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        //判断文件是否为空
        if (file == null) {
            return R.error("创建人脸模型失败，文件为空");
        }
        //判断是否以.jpg结尾
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + fileName;

        if (!fileName.endsWith(".jpg")){
            FileUtil.del(path);
            return R.error("签到失败，文件名必须以.JPG结尾");
        }else {
            //创建人脸模型
            try {
                file.transferTo(Paths.get(path)); //保存图片
                checkinService.createFaceModel(userId, path);
                return R.ok("创建人脸模型成功");
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EmosException("保存图片错误");
            } finally {
                FileUtil.del(path);
            }
        }
    }

}
