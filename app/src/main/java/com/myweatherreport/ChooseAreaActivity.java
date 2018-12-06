package com.myweatherreport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myweatherreport.db.City;
import com.myweatherreport.db.County;
import com.myweatherreport.db.Province;
import com.myweatherreport.util.HttpUtil;
import com.myweatherreport.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.view.View.GONE;

public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();


    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);

        titleText = (TextView) findViewById(R.id.title_text);
        backButton = (Button) findViewById(R.id.back_button);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        //初始化litepal
        LitePal.initialize(this);

        //设置逐次选择
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    //如果选择城市数量没达到上限，就传入城市Id
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    int fragmentCounter = prefs.getInt("total", 0);
                    if(fragmentCounter < 5) {
                        //判断当前城区是否已经选择
                        for(int i = 1; i <= fragmentCounter; i++){
                            if(weatherId.equals(prefs.getString("fragment" + Integer.toString(i) + "WeatherId", ""))) {
                                Toast.makeText(getApplicationContext(), "该地区已经选择", Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                if (i == fragmentCounter) {
                                    prefs.edit().putString("weatherId", weatherId).apply();
                                    Intent intent = new Intent(ChooseAreaActivity.this, MainActivity.class);
                                    intent.putExtra("choose_city_number", fragmentCounter + 1);
                                    startActivity(intent);
                                }
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "选择城市已经达到上限", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_PROVINCE) {
                    Intent intent = new Intent(ChooseAreaActivity.this, TotalCityListActivity.class);
                    startActivity(intent);
                }
            }
        });
        queryProvinces();
    }


    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.VISIBLE); //是否可见
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
                showProgressDialog();
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    if ("province".equals(type)) {
                                        queryProvinces();
                                    } else if ("city".equals(type)) {
                                        queryCities();
                                    } else if ("county".equals(type)) {
                                        queryCounties();
                                    }
                                }
                            });
                        }
                    }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
