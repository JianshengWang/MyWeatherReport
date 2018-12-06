package com.myweatherreport.viewpager;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myweatherreport.MainActivity;
import com.myweatherreport.R;

import java.util.List;

/**
 * Created by hp-pc on 2018/11/22.
 */

public class MyPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mfragmentList;
    private Context context;

    public MyPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, Context context) {
        super(fm);
        this.mfragmentList = fragmentList;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mfragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mfragmentList.size();
    }


}
