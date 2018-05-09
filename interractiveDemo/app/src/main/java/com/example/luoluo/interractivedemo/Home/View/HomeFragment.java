package com.example.luoluo.interractivedemo.Home.View;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.luoluo.interractivedemo.Enum.LiveItemType;
import com.example.luoluo.interractivedemo.Model.LiveModel;
import com.example.luoluo.interractivedemo.R;
import com.example.luoluo.interractivedemo.Adapter.BannerAdapter;
import com.example.luoluo.interractivedemo.Util.BannerViewPager;
import com.example.luoluo.interractivedemo.Adapter.LiveListRecyclerAdapter;
import com.example.luoluo.interractivedemo.Util.UIHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment implements ViewPager.OnPageChangeListener{

    public static final String TAG = "HomeFragment";
    public BannerViewPager mBannerPager;
    public RecyclerView mLiveListRecycler;
    public List<Fragment> imageFragments;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //通过LayoutInflater实列一个xml文件
    //使用布局填充器填充xml中的布局到container容器中
        View view = inflater.inflate(R.layout.fragment_home2,container,false);
        mBannerPager = view.findViewById(R.id.viewPager_banner);
        mLiveListRecycler = view.findViewById(R.id.recycler_liveList);
        imageFragments = new ArrayList<>();
        createBannerImageUI();
        initLiveListRecycleView();
        return view;
    }

    //banner图
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

    //直播列表
    public void initLiveListRecycleView(){


        //添加假数据
        Integer[] images = {R.drawable.icon_alpha,R.drawable.icon_monkon,R.drawable.icon_rabit};
        List<Integer>imageList = new ArrayList<>();
        imageList.add(R.drawable.banner_three);
        imageList.add(R.drawable.banner_one);
        imageList.addAll(Arrays.asList(images));

        Random random = new Random();
        String[] titles = {"最热直播","大司马来报道","胡歌来巡山","中原一点红"};
        ArrayList<LiveModel>models = new ArrayList<>();
        for (int i =0 ;i<12;i++){
            LiveModel model = new LiveModel();
            model.type = i==0?LiveItemType.LiveItemTypeTitle:
                    (i==1)?LiveItemType.LiveItemTypeFirst:LiveItemType.LiveItemTypeTitleSecond;
            model.imageIDS = (model.type == LiveItemType.LiveItemTypeFirst)?imageList:
                    imageList.subList(imageList.size()-3,imageList.size());
            model.titleNames = titles;
            models.add(model);
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mLiveListRecycler.setLayoutManager(layoutManager);

        LiveListRecyclerAdapter liveListRecyclerAdapter = new LiveListRecyclerAdapter(models);
        mLiveListRecycler.setAdapter(liveListRecyclerAdapter);
    }

    @Override
    public void onPageSelected(int position) {

//        Log.d(TAG, "onPageSelected: image position="+position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
