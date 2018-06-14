package com.example.luoluo.cartoon.UI;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.luoluo.cartoon.R;
import com.example.luoluo.cartoon.UI.Home.model.ItemModel;
import com.example.luoluo.cartoon.UI.Home.model.ItemModule;
import com.example.luoluo.cartoon.Utils.GlideImageLoader;
import com.example.luoluo.cartoon.Utils.Type.ItemModuleType;
import com.example.luoluo.cartoon.Utils.UIHelper;
import com.example.luoluo.cartoon.adapter.HomeListAdapter;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        this.setLiveListUI();
        //添加上拉和下拉刷新
        RefreshLayout refreshLayout = view.findViewById(R.id.refresh_home);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(2000);
            }
        });
        refreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadmore(2000);
            }
        });
    }
    private RecyclerView mRvLiveList;
    public void initView(View view){
        mRvLiveList =  (RecyclerView) view.findViewById(R.id.rv_live_list);
    }



    List<ItemModule> mModules;//模块数据
    HomeListAdapter mliveListAdapter;

    //假数据
    public  void setLiveListUI(){
        mModules = new ArrayList<>();

        //bannder数据
        ItemModule bannderModule = new ItemModule();
        bannderModule.modeuleType = ItemModuleType.Banner;
        mModules.add(bannderModule);

        String[] moduleTitles = {"热门推荐","最新热漫"};
        for (int i = 0;i<moduleTitles.length;i++){
            ItemModule module = new ItemModule();
            module.modeuleType = ItemModuleType.Title;
            module.title = moduleTitles[i];//模块标题
            mModules.add(module);

            for (int j= 0;j<1;j++){
                ItemModule module0 = new ItemModule();
                List<ItemModel> models3 = new ArrayList<>();
                if (i == 0){
                    //中间模块
                    module0.modeuleType = ItemModuleType.ConfereneItem;
                    int[] images3 = {R.drawable.default_banner,R.drawable.default_banner
                            ,R.drawable.default_banner};
                    String[] titles3 = {"第一个犬夜叉","第二个犬夜叉","第三个犬夜叉"};
                    for (int t= 0;t<images3.length;t++){
                        ItemModel model3 = new ItemModel();
                        model3.imageId=images3[t];
                        model3.title = titles3[t];
                        models3.add(model3);
                    }
                    module0.models = models3;
                }else if (i == 1){
                    //底部模块
                    module0.modeuleType = ItemModuleType.LiveItem;
                    int[] images3 = {R.drawable.default_banner};
                    for (int t= 0;t<images3.length;t++){
                        ItemModel model3 = new ItemModel();
                        model3.imageId=images3[t];
                        models3.add(model3);
                    }
                    module0.models = models3;
                }
                mModules.add(module0);

            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mRvLiveList.setLayoutManager(layoutManager);
        mliveListAdapter = new HomeListAdapter(this.getContext(),mModules);
        mRvLiveList.setAdapter(mliveListAdapter);

    }


}
