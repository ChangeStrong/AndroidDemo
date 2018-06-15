package com.example.luoluo.cartoon.UI.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.luoluo.cartoon.MainActivity;
import com.example.luoluo.cartoon.R;
import com.example.luoluo.cartoon.UI.Login.model.GuideData;
import com.example.luoluo.cartoon.UI.Login.view.SimpleGuideBanner;
import com.flyco.banner.anim.select.ZoomInEnter;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity {
    private Class<? extends ViewPager.PageTransformer> transformerClass;


    private List<GuideData> datas;
    private SimpleGuideBanner mBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);
        initData();
        initView();
    }

    private void initData() {
        datas = new ArrayList<>();
        datas.add(new GuideData(R.drawable.guide1, false));
        datas.add(new GuideData(R.drawable.guide2, false));
        datas.add(new GuideData(R.drawable.guide3, true));
    }

    private void initView() {
        mBanner = (SimpleGuideBanner) findViewById(R.id.sgb);
        mBanner
                .setIndicatorWidth(8)
                .setIndicatorHeight(8)
                .setIndicatorGap(12)
                .setIndicatorCornerRadius(3.5f)
                .setSelectAnimClass(ZoomInEnter.class)
                .setTransformerClass(transformerClass)
                .barPadding(0, 20, 0, 50)
                .setSource(datas)
                .startScroll();


        mBanner.setListener(new SimpleGuideBanner.OnEnterButtonClickListener() {
            @Override
            public void onEnterButtonClick(View v) {
                startActivity(new Intent(GuideActivity.this, PhoneLoginActivity.class));
                finish();
            }

        });
    }
}
