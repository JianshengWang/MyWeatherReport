package com.myweatherreport.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.myweatherreport.R;
import com.myweatherreport.TotalCityListActivity;
import com.myweatherreport.chart.ChartClass;
import com.myweatherreport.gson.Forecast;
import com.myweatherreport.gson.Weather;
import com.myweatherreport.util.HttpUtil;
import com.myweatherreport.util.Utility;
import com.myweatherreport.weathericon.WeatherIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by hp-pc on 2018/11/22.
 */

public class FifthFragment extends Fragment {

    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private String mWeatherId;
    private ImageView backgroundImage;
    private ImageView weatherIcon;

    private LineChart weatherChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fifth, container, false);

        //获得该碎片的城市天气Id
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String weatherId = prefs.getString("fragment5WeatherId", null);

/*** 初始化布局控件 ***/
        weatherLayout = view.findViewById(R.id.weather_layout_5);
        titleCity = view.findViewById(R.id.title_city_5);
        titleUpdateTime = view.findViewById(R.id.title_update_time_5);
        degreeText = view.findViewById(R.id.degree_text_5);
        weatherInfoText = view.findViewById(R.id.weather_info_text_5);
        forecastLayout = view.findViewById(R.id.forecast_layout_5);
        aqiText = view.findViewById(R.id.aqi_text_5);
        pm25Text = view.findViewById(R.id.pm25_text_5);
        comfortText = view.findViewById(R.id.comfort_text_5);
        carWashText = view.findViewById(R.id.car_wash_text_5);
        sportText = view.findViewById(R.id.sport_text_5);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_5);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        backgroundImage = view.findViewById(R.id.background_image_5);
        weatherIcon = view.findViewById(R.id.weather_icon_5);

        weatherChart = view.findViewById(R.id.weather_chart_5);
        ChartClass.initChart(weatherChart, weatherChart.getXAxis(), weatherChart.getAxisLeft(), weatherChart.getAxisRight(), weatherChart.getLegend(), -10);

        //获取网络状态，然后根据状态获取天气信息
        Context context = getActivity().getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String weatherString = prefs.getString("weather5", null);
        if (networkInfo == null) {
            if (weatherString != null) {
                Weather weather = Utility.handleWeatherResponse(weatherString);
                showWeatherInfo(weather);
                Toast.makeText(getActivity().getApplicationContext(), "当前加载缓存数据，请连接网络更新最新数据", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "当前无缓存数据也无网络连接，请联网更新最新数据", Toast.LENGTH_SHORT).show();
            }
        } else {
            mWeatherId = weatherId;
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });


        //加载背景图片
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.timg).asBitmap().into(backgroundImage);

        return view;
    }

    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=19b619f23b1646aa96fc629edcc78e48";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).edit();
                            editor.putString("weather5", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(getActivity().getBaseContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getBaseContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }


    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(final Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        //根据天气自动选择天气图标
        Glide.with(getActivity().getApplicationContext()).load(WeatherIcon.setWeatherIcon(weatherInfo)).asBitmap().into(weatherIcon);

        List<Entry> high = new ArrayList<>();//显示折线图的List
        List<Entry> low = new ArrayList<>();
        int i = 0;
        float lowestTemp = 70;
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText =  view.findViewById(R.id.max_text);
            TextView minText =  view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);

            int maxTemp, minTemp;
            maxTemp = Integer.parseInt(forecast.temperature.max);
            minTemp = Integer.parseInt(forecast.temperature.min);
            lowestTemp = minTemp < lowestTemp ? minTemp : lowestTemp;//获取最低温度
            Entry entryHigh = new Entry(i, maxTemp);
            Entry entryLow = new Entry(i, minTemp);
            high.add(entryHigh);
            low.add(entryLow);
            i++;
        }
        YAxis leftAxis = weatherChart.getAxisLeft();//设置y轴最小值
        leftAxis.setAxisMinimum(lowestTemp - 2);

        weatherChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                String tradeDate = weather.forecastList.get((int) v).date;
                return tradeDate;
            }
        });

        weatherChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                String data = (int)v + "℃";
                return data;
            }
        });

        LineDataSet lineDataSetHigh = new LineDataSet(high, "最高温度");
        LineDataSet lineDataSetLow = new LineDataSet(low, "最低温度");
        ChartClass.initLineDataSet(lineDataSetHigh, Color.RED, LineDataSet.Mode.CUBIC_BEZIER);
        ChartClass.initLineDataSet(lineDataSetLow, Color.WHITE, LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDataSetHigh, lineDataSetLow);
        weatherChart.setData(lineData);

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
