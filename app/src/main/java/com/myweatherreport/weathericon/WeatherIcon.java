package com.myweatherreport.weathericon;



import android.util.Log;

import com.myweatherreport.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by hp-pc on 2018/11/23.
 */

public class WeatherIcon {

    public static Integer setWeatherIcon(String weatherInfo) {

        //获取时间，来判断是白天还是晚上
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        Log.d("hour:", Integer.toString(hour));

        switch (weatherInfo){
            case "晴" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a100n;
                }else {
                    return R.drawable.a100;
                }
            case "多云" :
                return R.drawable.a101;
            case "少云" :
                return R.drawable.a102;
            case "晴间多云" :
                return R.drawable.a103;
            case "阴" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a104n;
                }else {
                    return R.drawable.a104;
                }
            case "有风" :
                return R.drawable.a200;
            case "平静" :
                return R.drawable.a201;
            case "微风" :
                return R.drawable.a202;
            case "和风" :
                return R.drawable.a203;
            case "清风" :
                return R.drawable.a204;
            case "强风" :
                return R.drawable.a205;
            case "疾风" :
                return R.drawable.a206;
            case "大风" :
                return R.drawable.a207;
            case "烈风" :
                return R.drawable.a208;
            case "风暴" :
                return R.drawable.a209;
            case "狂爆风" :
                return R.drawable.a210;
            case "飓风" :
                return R.drawable.a211;
            case "龙卷风" :
                return R.drawable.a212;
            case "热带暴风" :
                return R.drawable.a213;
            case "阵雨" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a300n;
                }else {
                    return R.drawable.a300;
                }
            case "强阵雨" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a301n;
                }else {
                    return R.drawable.a301;
                }
            case "雷阵雨" :
                return R.drawable.a302;
            case "强雷阵雨" :
                return R.drawable.a303;
            case "雷阵雨伴有冰雹" :
                return R.drawable.a304;
            case "小雨" :
                return R.drawable.a305;
            case "中雨" :
                return R.drawable.a306;
            case "大雨" :
                return R.drawable.a307;
            case "毛毛雨" :
                return R.drawable.a309;
            case "暴雨" :
                return R.drawable.a310;
            case "大暴雨" :
                return R.drawable.a311;
            case "特大暴雨" :
                return R.drawable.a312;
            case "冻雨" :
                return R.drawable.a313;
            case "小到中雨" :
                return R.drawable.a314;
            case "中到大雨" :
                return R.drawable.a315;
            case "大到暴雨" :
                return R.drawable.a316;
            case "暴雨到大暴雨" :
                return R.drawable.a317;
            case "大暴雨到特大暴雨" :
                return R.drawable.a318;
            case "雨" :
                return R.drawable.a399;
            case "小雪" :
                return R.drawable.a400;
            case "中雪" :
                return R.drawable.a401;
            case "大雪" :
                return R.drawable.a402;
            case "暴雪" :
                return R.drawable.a403;
            case "雨夹雪":
                return R.drawable.a404;
            case "雨雪天气" :
                return R.drawable.a405;
            case "阵雨夹雪" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a406n;
                }else {
                    return R.drawable.a406;
                }
            case "阵雪" :
                if((hour >18 && hour <24) || (hour > 0 && hour < 6)){
                    return R.drawable.a407n;
                }else {
                    return R.drawable.a407;
                }
            case "小到中雪" :
                return R.drawable.a408;
            case "中到大雪" :
                return R.drawable.a409;
            case "大到暴雪" :
                return R.drawable.a410;
            case "雪" :
                return R.drawable.a499;
            case "薄雾" :
                return R.drawable.a500;
            case "雾" :
                return R.drawable.a501;
            case "霾" :
                return R.drawable.a502;
            case "扬沙" :
                return R.drawable.a503;
            case "浮尘" :
                return R.drawable.a504;
            case "沙尘暴" :
                return R.drawable.a507;
            case "强沙尘暴" :
                return R.drawable.a508;
            case "浓雾" :
                return R.drawable.a509;
            case "强浓雾" :
                return R.drawable.a510;
            case "中度霾" :
                return R.drawable.a511;
            case "重度霾" :
                return R.drawable.a512;
            case "严重霾" :
                return R.drawable.a513;
            case "大雾" :
                return R.drawable.a514;
            case "特强浓雾" :
                return R.drawable.a515;
            case "热" :
                return R.drawable.a900;
            case "冷" :
                return R.drawable.a901;
            default:
                return R.drawable.a999;
        }
    }


}
