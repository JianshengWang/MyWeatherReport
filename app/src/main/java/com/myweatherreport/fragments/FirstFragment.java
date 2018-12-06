package com.myweatherreport.fragments;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myweatherreport.LocationApplication;
import com.myweatherreport.R;
import com.myweatherreport.chart.ChartClass;
import com.myweatherreport.db.City;
import com.myweatherreport.db.County;
import com.myweatherreport.db.Province;
import com.myweatherreport.gson.Forecast;
import com.myweatherreport.gson.Weather;
import com.myweatherreport.service.LocationService;
import com.myweatherreport.util.HttpUtil;
import com.myweatherreport.util.Utility;
import com.myweatherreport.weathericon.WeatherIcon;


import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by hp-pc on 2018/11/22.
 */

public class FirstFragment extends Fragment {

    private LocationService locationService;

    private Province selectedProvince;
    private City selectedCity;
    private String provinceName;
    private String cityName;
    private String countyName;
    private String weatherId;

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
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        // -----------location config ------------
        locationService = ((LocationApplication) getActivity().getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getActivity().getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();// 定位SDK
        // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request

        /*** 初始化布局控件 ***/
        weatherLayout = view.findViewById(R.id.weather_layout);
        titleCity = view.findViewById(R.id.title_city);
        titleUpdateTime = view.findViewById(R.id.title_update_time);
        degreeText = view.findViewById(R.id.degree_text);
        weatherInfoText = view.findViewById(R.id.weather_info_text);
        forecastLayout = view.findViewById(R.id.forecast_layout);
        aqiText = view.findViewById(R.id.aqi_text);
        pm25Text = view.findViewById(R.id.pm25_text);
        comfortText = view.findViewById(R.id.comfort_text);
        carWashText = view.findViewById(R.id.car_wash_text);
        sportText = view.findViewById(R.id.sport_text);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        backgroundImage = view.findViewById(R.id.background_image);
        weatherIcon = view.findViewById(R.id.weather_icon);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        weatherChart = view.findViewById(R.id.weather_chart);
        ChartClass.initChart(weatherChart, weatherChart.getXAxis(), weatherChart.getAxisLeft(), weatherChart.getAxisRight(), weatherChart.getLegend(), -10);

        //获取网络状态
        Context context = getActivity().getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String weatherString = prefs.getString("weather1", null);
        if (networkInfo == null) {
            if (weatherString != null) {
                Weather weather = Utility.handleWeatherResponse(weatherString);
                showWeatherInfo(weather);
                Toast.makeText(getActivity().getApplicationContext(), "当前加载缓存数据，请连接网络更新最新数据", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "当前无缓存数据也无网络连接，请联网更新最新数据", Toast.LENGTH_SHORT).show();
            }
        }


        //加载背景图片
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.timg).asBitmap().into(backgroundImage);

        return view;
    }


    /***
     * Stop location service
     */
    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }


    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */

                String longitude = Double.toString(location.getLongitude());//经度
                String latitude = Double.toString(location.getLatitude());//纬度
                final String getDistrictNameUrl = "https://search.heweather.com/find?location=" + longitude + "," + latitude + "&key=19b619f23b1646aa96fc629edcc78e48";
                HttpUtil.sendOkHttpRequest(getDistrictNameUrl, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText = response.body().string();
                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(responseText);
                        provinceName = jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("basic").getAsJsonArray().get(0).getAsJsonObject().get("admin_area").getAsString();
                        cityName = jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("basic").getAsJsonArray().get(0).getAsJsonObject().get("parent_city").getAsString();
                        countyName = jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("basic").getAsJsonArray().get(0).getAsJsonObject().get("location").getAsString();
                        getCountyWeatherId();//根据定位得到的信息获得weatherId
                    }
                });


                /*//省份
                String province = location.getProvince().replace("省", "");
                province = province.replace("市", "");
                province = province.replace("自治区", "");
                province = province.replace("壮族自治区", "");
                province = province.replace("回族自治区", "");
                province = province.replace("维吾尔自治区", "");
                provinceName = province;

                // 城市
                String city = location.getCity().replace("市", "");
                city = city.replace("地区", "");
                city = city.replace("哈萨克自治州", "");
                city = city.replace("蒙古族自治州", "");
                city = city.replace("回族自治州", "");
                city = city.replace("藏族自治州", "");
                city = city.replace("傈僳族自治州", "");
                city = city.replace("傣族景颇族自治州", "");
                city = city.replace("白族自治州", "");
                city = city.replace("傣族自治州", "");
                city = city.replace("壮族苗族自治州", "");
                city = city.replace("彝族自治州", "");
                city = city.replace("藏族羌族自治州", "");
                city = city.replace("土家族苗族自治州", "");
                city = city.replace("朝鲜族自治州", "");
                city = city.replace("盟", "");
                cityName = city;

                // 区
                String district = location.getDistrict().replace("区", "");
                district = district.replace("县", "");
                district = district.replace("市", "");
                district = district.replace("新区", "");
                district = district.replace("满族自治县", "");
                district = district.replace("矿区", "");
                district = district.replace("蒙古族自治县", "");
                district = district.replace("回族自治县", "");
                district = district.replace("林区", "");
                district = district.replace("坪区", "");
                district = district.replace("岭区", "");
                district = district.replace("旗", "");
                district = district.replace("联合旗", "");
                district = district.replace("湾区", "");
                district = district.replace("朝鲜族自治县", "");
                district = district.replace("达斡尔族自治县", "");
                district = district.replace("土家族自治县", "");
                district = district.replace("苗族自治县", "");
                district = district.replace("瑶族自治县", "");
                district = district.replace("侗族自治县", "");
                district = district.replace("各族自治县", "");
                district = district.replace("港区", "");
                district = district.replace("南族自治县", "");
                district = district.replace("群岛", "");
                district = district.replace("黎族自治县", "");
                district = district.replace("羌族自治县", "");
                district = district.replace("彝族自治县", "");
                district = district.replace("藏族自治县", "");
                countyName = district;*/

                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    Toast.makeText(getContext(), "gps定位成功", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    Toast.makeText(getContext(), "网络定位成功", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    Toast.makeText(getContext(), "离线定位成功，离线定位结果也是有效的", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(getContext(), "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(getContext(), "网络不同导致定位失败，请检查网络是否通畅", Toast.LENGTH_LONG).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(getContext(), "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机", Toast.LENGTH_LONG).show();
                }
            }
        }

    };

    /***
     *
     *
     * 以下为通过定位获得的城市名获取城区天气Id程序段
     *
     */

    private void getCountyWeatherId() {
        List<Province> provinceList;
        List<City> cityList;
        List<County> countyList;
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            for (Province province : provinceList) {
                if(province.getProvinceName().equals(provinceName)) {  //如果循环到的省名和输入省名相同
                    selectedProvince = province;
                    cityList = DataSupport.where("provinceid = ?", Integer.toString(province.getProvinceCode())).find(City.class);
                    if(cityList.size() > 0) {
                        for (City city : cityList) {
                            if(city.getCityName().equals(cityName)) {
                                selectedCity = city;
                                countyList = DataSupport.where("cityid = ?", Integer.toString(city.getCityCode())).find(County.class);
                                if (countyList.size() > 0){
                                    for (County county : countyList) {
                                        if (county.getCountyName().equals(countyName)) {
                                            //获取区县天气id
                                            weatherId = county.getWeatherId();
                                            requestWeather(weatherId);//如果成功定位并获取weatherId，就显示天气
                                        }
                                    }
                                } else {
                                    String address = "http://guolin.tech/api/china/" + province.getProvinceCode() + "/" + city.getCityCode();
                                    queryFromServer(address, "county");
                                }
                            }
                        }
                    } else {
                        String address = "http://guolin.tech/api/china/" + province.getProvinceCode();
                        queryFromServer(address, "city");
                    }
                }
            }
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCountyWeatherId();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /***
     *
     * 以下为加载天气程序段
     *
     */

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
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                            editor.putString("weather1", responseText);
                            editor.putString("fragment1WeatherId", weatherId);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
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

        //加载进入通知变量
        /*SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editor.putInt("notificationImage", WeatherIcon.setWeatherIcon(weatherInfo));
        editor.putString("notificationDistrict", cityName);
        editor.putString("notificationDegree", degree);
        editor.putString("notificationWeather", weatherInfo);
        editor.apply();*/

        List<Entry> high = new ArrayList<>();//显示折线图的List
        List<Entry> low = new ArrayList<>();
        int i = 0;
        float lowestTemp = 0;
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
