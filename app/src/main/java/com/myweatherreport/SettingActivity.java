package com.myweatherreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class SettingActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private Switch updateSwitch;
    public boolean updateChoise;

    private EditText updateBlankEdit;
    public int updateBlankTime;

    private Switch notifySwitch;
    public boolean notifyChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        updateSwitch = (Switch) findViewById(R.id.update_switch);
        updateBlankEdit = (EditText) findViewById(R.id.update_blank_setting);
        notifySwitch = (Switch) findViewById(R.id.notify_switch);

        toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //获取设置状态
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        updateBlankTime = pref.getInt("update_time", 0);
        updateChoise = pref.getBoolean("service_switch", false);
        notifyChoice = pref.getBoolean("notify_switch", false);

        //设置服务开关内容
        updateSwitch.setChecked(updateChoise);
        updateSwitch.setSwitchTextAppearance(SettingActivity.this,R.style.s_false);
        updateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                //控制开关字体颜色
                if (b) {
                    updateSwitch.setSwitchTextAppearance(SettingActivity.this,R.style.s_true);
                    editor.putBoolean("service_switch", true);
                }else {
                    updateSwitch.setSwitchTextAppearance(SettingActivity.this,R.style.s_false);
                    editor.putBoolean("service_switch", false);
                }
                editor.apply();
            }
        });

        //设置更新时间
        updateBlankEdit.setText(Integer.toString(updateBlankTime));

        //设置通知开关内容
        notifySwitch.setChecked(notifyChoice);
        notifySwitch.setSwitchTextAppearance(SettingActivity.this, R.style.s_false);
        notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                //控制开关字体颜色
                if (b) {
                    updateSwitch.setSwitchTextAppearance(SettingActivity.this,R.style.s_true);
                    editor.putBoolean("notify_switch", true);
                }else {
                    updateSwitch.setSwitchTextAppearance(SettingActivity.this,R.style.s_false);
                    editor.putBoolean("notify_switch", false);
                }
                editor.apply();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (updateBlankEdit.getText() != null) {
            pref.edit().putInt("update_time", Integer.parseInt(updateBlankEdit.getText().toString())).apply();
        }

        //重启服务
        Intent intent = new Intent(this, UpdateService.class);
        if(!pref.getBoolean("service_switch", false)) {
            stopService(intent);
        } else if (pref.getBoolean("service_switch", false)) {
            startService(intent);
        }
    }
}
