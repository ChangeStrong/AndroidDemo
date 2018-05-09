package com.example.luoluo.interractivedemo;

import android.databinding.DataBindingUtil;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;

import com.example.luoluo.interractivedemo.Home.View.HomeFragment;
import com.example.luoluo.interractivedemo.Home.View.MeFragment;
import com.example.luoluo.interractivedemo.Home.View.MessageListFragment;
import com.example.luoluo.interractivedemo.Model.User;
import com.example.luoluo.interractivedemo.Util.HomeViewPager;
import com.example.luoluo.interractivedemo.Adapter.LLHomePagerAdapter;
import com.example.luoluo.interractivedemo.Util.UIHelper;
import com.example.luoluo.interractivedemo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,TabLayout.OnTabSelectedListener{

    public static final String TAG = "MainActivity";

    public TabLayout mTabLayout;
    private HomeViewPager mPager;
    //每一个界面
    private List<Fragment> mViews;
    ActivityMainBinding mMainBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: screenWidth="+UIHelper.getScreenWidth()+"screenHeight="+UIHelper.getScreenHeight());
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        User user = new User("ChangeStrong", "luoluo");
        mMainBinding.setUserModel(user);
        mViews = new ArrayList<Fragment>();
        mPager = findViewById(R.id.viewPager_main);
//      mPager = mMainBinding.viewPagerMain;

        //初始化viewPager
        initPageView();
        initTabLayoutView();
    }

    public void initPageView(){
        HomeFragment homeFragment = new HomeFragment();
        MessageListFragment messageListFragment = new MessageListFragment();
        MeFragment meFragment = new MeFragment();
//        downFragment.getView().setBackgroundColor(Color.RED);

        mViews.add(homeFragment);
        mViews.add(messageListFragment);
        mViews.add(meFragment);

        mPager.homeFragment = homeFragment;
        //数据传入viewpage
        LLHomePagerAdapter homePagerAdapter = new LLHomePagerAdapter(getSupportFragmentManager(),mViews);

        //new  LLHomePagerAdapter(mViews);
        mPager.setAdapter(homePagerAdapter);
        mPager.setCurrentItem(0);  //初始化显示第一个页面
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        mPager.setOffscreenPageLimit(3);
        mPager.addOnPageChangeListener(this);

    }

    public void initTabLayoutView(){
        mTabLayout = findViewById(R.id.tabLayout_main);
        mTabLayout.addOnTabSelectedListener(this);
        //tabLayout.setupWithViewPager(mPager);


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        mTabLayout.setScrollPosition(position,position/mTabLayout.getTabCount(),
                true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: "+position);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
       int position = mTabLayout.getSelectedTabPosition();
        Log.d(TAG, "onTabSelected: position="+position);
        mPager.setCurrentItem(position);

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }
}

