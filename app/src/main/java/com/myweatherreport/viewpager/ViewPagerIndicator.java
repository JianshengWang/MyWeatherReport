package com.myweatherreport.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.myweatherreport.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp-pc on 2018/11/28.
 */

public class ViewPagerIndicator implements ViewPager.OnPageChangeListener {
    private Context context;
    private ViewPager viewPager;
    private LinearLayout dotLayout;
    private int size;
    private int img1 = R.mipmap.gouwuzy_16, img2 = R.mipmap.gouwuzy_17;
    private int imgSize = 15;
    private List<ImageView> dotViewLists = new ArrayList<>();

    public ViewPagerIndicator(Context context, ViewPager viewPager, LinearLayout dotLayout, int size) {
        this.context = context;
        this.viewPager = viewPager;
        this.dotLayout = dotLayout;
        this.size = size;

        for(int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            //小圆点左右间隔
            params.leftMargin = 10;
            params.rightMargin = 10;
            //设置小圆点大小
            params.height = imgSize;
            params.width = imgSize;
            if (i == 0) {
                imageView.setBackgroundResource(img1);
            } else {
                imageView.setBackgroundResource(img2);
            }
            //给LinearLayout添加ImageView
            dotLayout.addView(imageView, params);
            dotViewLists.add(imageView);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < size; i++) {
            if ((position % size) == i) {
                ((View) dotViewLists.get(i)).setBackgroundResource(img1);
            } else {
                ((View) dotViewLists.get(i)).setBackgroundResource(img2);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
