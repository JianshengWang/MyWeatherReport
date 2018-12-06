package com.myweatherreport;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    //申请权限
    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;

    ImageView startImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        startImage=(ImageView)findViewById(R.id.start_image);

        //判断是否是第一次启动app
        SharedPreferences setting = getSharedPreferences("is", MODE_PRIVATE);
        Boolean user_first = setting.getBoolean("FIRST", true);
        if(user_first) {//第一次
            setting.edit().putBoolean("FIRST", false).apply();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            //设置初始页面数量为1
            editor.putInt("total",1);
            //设置初始设置参数
            editor.putBoolean("service_switch", false);
            editor.putInt("update_time", 1);
            editor.putBoolean("notify_switch", false);
            editor.apply();
        }

        //获取相关权限
        getPermissions();

        //加载通知
        /*NotificationManagerCompat.from(this).areNotificationsEnabled();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Integer notificationImage = prefs.getInt("notificationImage", 0);
        String notificationDistrict = prefs.getString("notificationDistrict", "");
        String notificationWeather = prefs.getString("notificationWeather", "");
        String notificationDegree = prefs.getString("notificationDegree", "");
        if(notificationImage != 0) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(notificationDistrict)
                    .setContentText(notificationWeather + notificationDegree)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(notificationImage)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationImage))
                    .setContentIntent(pi)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(), notificationImage)))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            manager.notify(1, notification);
        }*/
    }

    @Override
    protected void onStart(){
        super.onStart();
        initImage();
    }

    private void initImage(){
        startImage.setImageResource(R.drawable.startimage);

        //进行缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.4f, 1.0f, 1.4f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(4000);
        //动画播放完成后保持形状
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //可以在这里先进行某些操作
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //判断必需权限是否通过
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        ||  checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ||  checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "请允许定位及存储权限", Toast.LENGTH_LONG).show();
                }else {
                    startActivity();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startImage.startAnimation(scaleAnimation);
    }

    private void startActivity() {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @TargetApi(23)
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
