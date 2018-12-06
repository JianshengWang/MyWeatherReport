package com.myweatherreport;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.myweatherreport.gson.Weather;
import com.myweatherreport.util.HttpUtil;
import com.myweatherreport.util.Utility;
import com.myweatherreport.weathericon.WeatherIcon;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateService extends Service {

    RemoteViews remoteViews;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int updateBlankTime = pref.getInt("update_time", 0);

        //设置自定义通知
        remoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);

        updateWeather();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather1", null);
        boolean notifyChoice = pref.getBoolean("notify_switch", false);
        if(weatherString != null && notifyChoice) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showNotify(weather);
        }

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int aMinuate = updateBlankTime * 60 * 1000;  //更新周期几分钟
        long triggerAtTime = SystemClock.elapsedRealtime() + aMinuate;
        Intent i = new Intent(this, UpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /***
     * 显示通知
     */
    private void showNotify(Weather weather) {

        //自定义通知内容设置
        remoteViews.setImageViewResource(R.id.notification_image, WeatherIcon.setWeatherIcon(weather.now.more.info));
        remoteViews.setTextViewText(R.id.notification_title, weather.basic.cityName);
        remoteViews.setTextViewText(R.id.notification_temp, "气温：" + weather.now.temperature + "℃");
        remoteViews.setTextViewText(R.id.notification_AQI, "空气指数：" + weather.aqi.city.aqi);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notificationLayout, pendingIntent);

        //获取系统通知服务
        String id = "my_channel_01";
        String name="我是渠道名字";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .setSmallIcon(WeatherIcon.setWeatherIcon(weather.now.more.info))
                    .setContentIntent(pendingIntent)
                    .setContent(remoteViews)
                    .build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setChannel(id)
                    .setSmallIcon(WeatherIcon.setWeatherIcon(weather.now.more.info))
                    .setContentIntent(pendingIntent)
                    .setContent(remoteViews)
                    .setOngoing(true);//无效
            notification = notificationBuilder.build();
        }
        notificationManager.notify(111123, notification);
    }

    /***
     * 更新第一个城市的天气
     */
    private void updateWeather() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather1", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=19b619f23b1646aa96fc629edcc78e48";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateService.this).edit();
                        editor.putString("weather1", responseText);
                        editor.apply();
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


}
