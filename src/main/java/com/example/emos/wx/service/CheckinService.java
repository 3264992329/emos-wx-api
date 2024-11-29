package com.example.emos.wx.service;

import java.util.HashMap;

public interface CheckinService {
    public String validCanCheckIn(int UserId, String data);
    public void checkIn(HashMap param);
    public void createFaceModel(int UserId, String path);
}
