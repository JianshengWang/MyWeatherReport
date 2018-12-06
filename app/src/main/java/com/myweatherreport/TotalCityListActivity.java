package com.myweatherreport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.myweatherreport.db.WeatherList;
import com.myweatherreport.gson.Weather;
import com.myweatherreport.util.Utility;
import com.myweatherreport.weathericon.WeatherIcon;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class TotalCityListActivity extends AppCompatActivity {

    ListView listView;
    private List<WeatherList> weatherList = new ArrayList<>();
    WeatherListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_city_list);

        listView = (ListView) findViewById(R.id.total_city_list_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_city_list);
        toolbar.setTitle("城市列表");
        setSupportActionBar(toolbar);

        SharedPreferences pref = getSharedPreferences("FragmentCounter", MODE_PRIVATE);
        int fragmentCounter = pref.getInt("counter", 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < fragmentCounter; i++) {
            String weatherString = prefs.getString("weather" + Integer.toString(i+1), null);
            if(weatherString != null) {
                Weather weather = Utility.handleWeatherResponse(weatherString);
                WeatherList cityWeather = new WeatherList(i, weather.basic.cityName, weather.now.more.info, weather.now.temperature + "℃");
                weatherList.add(cityWeather);
            }
        }

        adapter = new WeatherListAdapter(TotalCityListActivity.this, R.layout.total_city_list_item, weatherList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WeatherList weatherList1 = weatherList.get(position);
                int listMember = weatherList1.getId() + 1;
                Intent intent = new Intent(TotalCityListActivity.this, MainActivity.class);
                intent.putExtra("list_id", listMember);
                startActivity(intent);
                finish();
            }
        });

        //注册上下文菜单
        registerForContextMenu(listView);
    }

    //加载menu文件夹里的上下文菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.total_city_list_view) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.context_menu, menu);
        }
    }

    //设置上下文菜单点击后事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.delete:
                    switch (menuInfo.position) {
                        case 0:
                            Toast.makeText(this, "本地天气页面不可删除", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Intent intent = new Intent();
                            intent.setClass(TotalCityListActivity.this, MainActivity.class);
                            intent.putExtra("delete_fragment", menuInfo.position + 1);
                            startActivity(intent);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        return  super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        weatherList.clear();
        SharedPreferences pref = getSharedPreferences("FragmentCounter", MODE_PRIVATE);
        int fragmentCounter = pref.getInt("counter", 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < fragmentCounter; i++) {
            String weatherString = prefs.getString("weather" + Integer.toString(i+1), null);
            if(weatherString != null) {
                Weather weather = Utility.handleWeatherResponse(weatherString);
                WeatherList cityWeather = new WeatherList(i, weather.basic.cityName, weather.now.more.info, weather.now.temperature + "℃");
                weatherList.add(cityWeather);
            }
        }
        adapter.notifyDataSetChanged();
    }


    //toolbar点击事件的设置
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            Intent intent = new Intent(TotalCityListActivity.this, ChooseAreaActivity.class);
            startActivity(intent);
        }
        return true;
    }



    //Listview Adapter
    class WeatherListAdapter extends ArrayAdapter<WeatherList> {
        private int resourceId;

        public WeatherListAdapter(Context context, int textViewResourceId, List<WeatherList> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WeatherList weatherList = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView cityListCityName = view.findViewById(R.id.city_list_cityname);
            ImageView cityListWeatherIcon = view.findViewById(R.id.city_list_weather_icon);
            TextView cityListWeather = view.findViewById(R.id.city_list_weather);
            TextView cityListTemperature = view.findViewById(R.id.city_list_temperature);

            cityListCityName.setText(weatherList.getCityName());
            cityListTemperature.setText(weatherList.getTemperature());
            cityListWeather.setText(weatherList.getWeatherInfo());
            Glide.with(getApplicationContext()).load(WeatherIcon.setWeatherIcon(weatherList.getWeatherInfo())).asBitmap().into(cityListWeatherIcon);
            return view;
        }
    }

}