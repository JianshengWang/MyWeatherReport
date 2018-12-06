package com.myweatherreport;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;
import com.myweatherreport.service.LocationService;

/**
 * Created by hp-pc on 2018/11/21.
 */

public class LocationApplication extends Application {

    public LocationService locationService;
    public Vibrator mVibrator;
    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());

    }

}
