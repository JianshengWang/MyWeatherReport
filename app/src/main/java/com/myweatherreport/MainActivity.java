package com.myweatherreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.myweatherreport.fragments.FifthFragment;
import com.myweatherreport.fragments.FirstFragment;
import com.myweatherreport.fragments.ForthFragment;
import com.myweatherreport.fragments.SecondFragment;
import com.myweatherreport.fragments.ThirdFragment;
import com.myweatherreport.viewpager.MyPagerAdapter;
import com.myweatherreport.viewpager.ViewPagerIndicator;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //建立碎片对象和viewpager对象
    private ViewPager viewPager;
    private MyPagerAdapter adapter;
    private FirstFragment firstFragment;
    private SecondFragment secondFragment;
    private ThirdFragment thirdFragment;
    private ForthFragment forthFragment;
    private FifthFragment fifthFragment;
    private List<Fragment> fragmentList = new ArrayList<>();

    //设置viewpager下方小圆点对象
    private LinearLayout viewpagerLinearLayout;

    //设置左侧按键
    private ImageView listviewButton;

    //设置右侧按键
    private ImageView settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        listviewButton = (ImageView) findViewById(R.id.list_view_button);
        listviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TotalCityListActivity.class);
                startActivity(intent);
            }
        });

        settingButton = (ImageView) findViewById(R.id.list_view_setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        //设置viewpager
        viewPager=(ViewPager)findViewById(R.id.viewpager);
        firstFragment = new FirstFragment();
        secondFragment = new SecondFragment();
        thirdFragment = new ThirdFragment();
        forthFragment = new ForthFragment();
        fifthFragment = new FifthFragment();
        fragmentList.add(firstFragment);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int fragmentCounter = prefs.getInt("total", 0);
        switch (fragmentCounter) {
            case 2:
                fragmentList.add(secondFragment);
                break;
            case 3:
                fragmentList.add(secondFragment);
                fragmentList.add(thirdFragment);
                break;
            case 4:
                fragmentList.add(secondFragment);
                fragmentList.add(thirdFragment);
                fragmentList.add(forthFragment);
                break;
            case 5:
                fragmentList.add(secondFragment);
                fragmentList.add(thirdFragment);
                fragmentList.add(forthFragment);
                fragmentList.add(fifthFragment);
                break;
            default:
                break;
        }

        //设置小圆点
        viewpagerLinearLayout = (LinearLayout) findViewById(R.id.viewpager_linerlayout);
        viewPager.setOnPageChangeListener(new ViewPagerIndicator(this, viewPager, viewpagerLinearLayout, fragmentList.size()));

        adapter = new MyPagerAdapter(getSupportFragmentManager(),fragmentList, this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setCurrentItem(0);


        SharedPreferences.Editor editor = getSharedPreferences("FragmentCounter", MODE_PRIVATE).edit();
        editor.putInt("counter", fragmentList.size());
        editor.apply();

        //初始化litepal
        LitePal.initialize(this);

        boolean updateChoise = prefs.getBoolean("service_switch", false);
        //开启服务
        if (updateChoise) {
            Intent intent = new Intent(this, UpdateService.class);
            startService(intent);
        }

    }

    protected void onResume() {
        super.onResume();
        //选择城市跳转后添加城市Fragment
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectWeatherId = prefs.getString("weatherId", null);
        if(fragmentList.size() < 5 && selectWeatherId != null) {
            addFragment(selectWeatherId);
            adapter.notifyDataSetChanged();
            //动态改变小圆点的个数，先清空LinearLayout，然后重新添加小圆点
            viewpagerLinearLayout.removeAllViews();
            viewPager.setOnPageChangeListener(new ViewPagerIndicator(this, viewPager, viewpagerLinearLayout, fragmentList.size()));
            selectWeatherId = null;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString("weatherId", null);
            editor.apply();
        }

        //更新碎片数量
        SharedPreferences.Editor editor = getSharedPreferences("FragmentCounter", MODE_PRIVATE).edit();
        editor.putInt("counter", fragmentList.size());
        editor.apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //在列表中点击列表选项跳转到MainActivity后，选择进入哪个Fragment
        int id = intent.getIntExtra("list_id", 0);
        if(id >= 1 && id <= 5) {
            viewPager.setCurrentItem(id - 1);
        }


        //删除城市碎片程序段
        int deleteFragment = intent.getIntExtra("delete_fragment", 0);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int fragmentNumber = pref.getInt("total", 0);
        //如果删除的是最后一个，直接在列表里删除碎片，然后刷新viewpager即可
        //如果删除的不是最后一个，则先把后面的错位到前面，然后删除最后一个
        /*if(fragmentNumber == 2 && deleteFragment == 2) {
            fragmentList.remove(secondFragment);
            pref.edit().putInt("total", 1).apply();
            pref.edit().putString("fragment2WeatherId", "").apply();
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 3 && deleteFragment == 2){
            pref.edit().putString("fragment2WeatherId", pref.getString("fragment3WeatherId", "")).apply();
            pref.edit().putInt("total", 2).apply();
            pref.edit().putString("fragment3WeatherId", "").apply();
            fragmentList.remove(thirdFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 3 && deleteFragment == 3) {
            fragmentList.remove(thirdFragment);
            pref.edit().putInt("total", 2).apply();
            pref.edit().putString("fragment3WeatherId", "").apply();
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 4 && deleteFragment == 2){
            pref.edit().putString("fragment2WeatherId", pref.getString("fragment3WeatherId", "")).apply();
            pref.edit().putString("fragment3WeatherId", pref.getString("fragment4WeatherId", "")).apply();
            pref.edit().putInt("total", 3).apply();
            pref.edit().putString("fragment4WeatherId", "").apply();
            fragmentList.remove(forthFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 4 && deleteFragment == 3){
            pref.edit().putString("fragment3WeatherId", pref.getString("fragment4WeatherId", "")).apply();
            pref.edit().putInt("total", 3).apply();
            pref.edit().putString("fragment4WeatherId", "").apply();
            fragmentList.remove(forthFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 4 && deleteFragment == 4) {
            fragmentList.remove(forthFragment);
            pref.edit().putInt("total", 3).apply();
            pref.edit().putString("fragment4WeatherId", "").apply();
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 5 && deleteFragment == 2){
            pref.edit().putString("fragment2WeatherId", pref.getString("fragment3WeatherId", "")).apply();
            pref.edit().putString("fragment3WeatherId", pref.getString("fragment4WeatherId", "")).apply();
            pref.edit().putString("fragment4WeatherId", pref.getString("fragment5WeatherId", "")).apply();
            pref.edit().putInt("total", 4).apply();
            pref.edit().putString("fragment5WeatherId", "").apply();
            fragmentList.remove(fifthFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 5 && deleteFragment == 3){
            pref.edit().putString("fragment3WeatherId", pref.getString("fragment4WeatherId", "")).apply();
            pref.edit().putString("fragment4WeatherId", pref.getString("fragment5WeatherId", "")).apply();
            pref.edit().putInt("total", 4).apply();
            pref.edit().putString("fragment5WeatherId", "").apply();
            fragmentList.remove(fifthFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 5 && deleteFragment == 4){
            pref.edit().putString("fragment4WeatherId", pref.getString("fragment5WeatherId", "")).apply();
            pref.edit().putInt("total", 4).apply();
            pref.edit().putString("fragment5WeatherId", "").apply();
            fragmentList.remove(fifthFragment);
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        } else if(fragmentNumber == 5 && deleteFragment == 5) {
            fragmentList.remove(fifthFragment);
            pref.edit().putInt("total", 4).apply();
            pref.edit().putString("fragment5WeatherId", "").apply();
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        }*/
        if (deleteFragment >=2 && deleteFragment <= 5) {
            switch (fragmentNumber) {
                case 2:
                    fragmentList.remove(secondFragment);
                    pref.edit().putInt("total", 1).apply();
                    pref.edit().putString("fragment2WeatherId", "").apply();
                    pref.edit().putString("weather2", "").apply();
                    break;
                case 3:
                    for (int i = 0; i < fragmentNumber - deleteFragment; i++) {
                        pref.edit().putString("fragment" + Integer.toString(deleteFragment + i) + "WeatherId", pref.getString("fragment" + Integer.toString(deleteFragment + i + 1) + "WeatherId", "")).apply();
                        pref.edit().putString("weather" + Integer.toString(deleteFragment + i), pref.getString("weather" + Integer.toString(deleteFragment + i + 1), "")).apply();
                    }
                    pref.edit().putInt("total", 2).apply();
                    pref.edit().putString("fragment3WeatherId", "").apply();
                    pref.edit().putString("weather3", "").apply();
                    fragmentList.remove(thirdFragment);
                    break;
                case 4:
                    for (int i = 0; i < fragmentNumber - deleteFragment; i++) {
                        pref.edit().putString("fragment" + Integer.toString(deleteFragment + i) + "WeatherId", pref.getString("fragment" + Integer.toString(deleteFragment + i + 1) + "WeatherId", "")).apply();
                        pref.edit().putString("weather" + Integer.toString(deleteFragment + i), pref.getString("weather" + Integer.toString(deleteFragment + i + 1), "")).apply();
                    }
                    pref.edit().putInt("total", 3).apply();
                    pref.edit().putString("fragment4WeatherId", "").apply();
                    pref.edit().putString("weather4", "").apply();
                    fragmentList.remove(forthFragment);
                    break;
                case 5:
                    for (int i = 0; i < fragmentNumber - deleteFragment; i++) {
                        pref.edit().putString("fragment" + Integer.toString(deleteFragment + i) + "WeatherId", pref.getString("fragment" + Integer.toString(deleteFragment + i + 1) + "WeatherId", "")).apply();
                        pref.edit().putString("weather" + Integer.toString(deleteFragment + i), pref.getString("weather" + Integer.toString(deleteFragment + i + 1), "")).apply();
                    }
                    pref.edit().putInt("total", 4).apply();
                    pref.edit().putString("fragment5WeatherId", "").apply();
                    pref.edit().putString("weather5", "").apply();
                    fragmentList.remove(fifthFragment);
                    break;
            }
            adapter.notifyDataSetChanged();
            viewPager.setAdapter(adapter);
        }

        //更新碎片数量
        SharedPreferences.Editor editor = getSharedPreferences("FragmentCounter", MODE_PRIVATE).edit();
        editor.putInt("counter", fragmentList.size());
        editor.apply();

        //动态改变小圆点的个数，先清空LinearLayout，然后重新添加小圆点
        viewpagerLinearLayout.removeAllViews();
        viewPager.setOnPageChangeListener(new ViewPagerIndicator(this, viewPager, viewpagerLinearLayout, fragmentList.size()));

    }


    //添加一个城市
    private void addFragment(String weatherId){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        int fragmentNumber=fragmentList.size();
        if(fragmentNumber < 5){
            switch (fragmentNumber){
                case 1:fragmentList.add(secondFragment);
                    editor.putInt("total",2);
                    editor.putString("fragment2WeatherId", weatherId);
                    editor.apply();
                    break;
                case 2:fragmentList.add(thirdFragment);
                    editor.putInt("total",3);
                    editor.putString("fragment3WeatherId", weatherId);
                    editor.apply();
                    break;
                case 3:fragmentList.add(forthFragment);
                    editor.putInt("total",4);
                    editor.putString("fragment4WeatherId", weatherId);
                    editor.apply();
                    break;
                case 4:fragmentList.add(fifthFragment);
                    editor.putInt("total",5);
                    editor.putString("fragment5WeatherId", weatherId);
                    editor.apply();
                    break;
            }
        }else{
            Toast.makeText(this,"添加城市数量已达上限",Toast.LENGTH_SHORT).show();
        }
    }



}
