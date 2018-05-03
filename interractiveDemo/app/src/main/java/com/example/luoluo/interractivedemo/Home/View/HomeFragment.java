package com.example.luoluo.interractivedemo.Home.View;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.luoluo.interractivedemo.R;
import com.example.luoluo.interractivedemo.Util.BannerAdapter;
import com.example.luoluo.interractivedemo.Util.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ViewPager.OnPageChangeListener{

    public static final String TAG = "HomeFragment";
    public ViewPager mBannerPager;
    public List<Fragment> imageFragments;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home2,container,false);
        mBannerPager = view.findViewById(R.id.viewPager_banner);
        imageFragments = new ArrayList<>();
        createBannerImageUI();
        return view;
    }

    //添加适配器和传入数据
    public void createBannerImageUI(){

        //动态设置宽高比例
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) mBannerPager.getLayoutParams();
        linearParams.height = (int) (UIHelper.getScreenWidth()*(667.0/1000.0));
        mBannerPager.setLayoutParams(linearParams);

        //banner图片数组传入
        int[] list = new int[]{R.drawable.banner_one, R.drawable.banner_two,
                R.drawable.banner_three, R.drawable.banner_four};
        for (int i=0;i<list.length;i++){
            ImageFragment imageFragment = new ImageFragment();
            imageFragment.imageSourceid = list[i];
            imageFragments.add(imageFragment);
        }
        //数据传入viewpager
        BannerAdapter homePagerAdapter = new BannerAdapter(getFragmentManager(),imageFragments); //new  LLHomePagerAdapter(mViews);
        mBannerPager.setAdapter(homePagerAdapter);
        mBannerPager.setCurrentItem(0);  //选中第一个界面
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        mBannerPager.setOffscreenPageLimit(3);
        mBannerPager.addOnPageChangeListener(this);

    }

    @Override
    public void onPageSelected(int position) {

        Log.d(TAG, "onPageSelected: image position="+position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
