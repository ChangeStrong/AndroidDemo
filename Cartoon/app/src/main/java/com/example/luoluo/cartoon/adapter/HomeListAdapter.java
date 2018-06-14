package com.example.luoluo.cartoon.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.luoluo.cartoon.R;
import com.example.luoluo.cartoon.UI.Home.model.ItemModule;
import com.example.luoluo.cartoon.Utils.GlideImageLoader;
import com.example.luoluo.cartoon.Utils.Type.ItemModuleType;
import com.example.luoluo.cartoon.Utils.UIHelper;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class HomeListAdapter extends RecyclerView.Adapter {
    public String TAG = "HomeLiveListAdapter";
    private List<ItemModule> modules;
    private Context mContext;
    public HomeListAdapter(Context context, List<ItemModule> moduleList) {
        mContext = context;
        modules = moduleList;
    }

    @Override
    public int getItemViewType(int position) {
        ItemModule model = modules.get(position);
         Log.d(TAG, "getItemViewType: ="+" position="+position +" oridinal" +model.modeuleType.ordinal()+" type="+model.modeuleType);
        return  model.modeuleType.ordinal();//枚举转int

    }
    private Banner mBanner;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (ItemModuleType.values()[viewType]){
            case Title:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.module_home_mid_title,parent,false);
                return new TitleModuleHolder(view);
            case ConfereneItem:
            case LiveItem:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.module_home_mid,parent,false);
                return new LiveModuleHolder(view);
            case Banner:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.module_home_banner,parent,false);
                return new BannerModuleHoler(view);
            default:
                break;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseModuleHolder holder1 = (BaseModuleHolder) holder;
        //到子类去执行
        holder1.onBindViewHolder(holder, position);

    }

    @Override
    public int getItemCount() {

        return modules.size();
    }

    public class BaseModuleHolder extends RecyclerView.ViewHolder{

        public BaseModuleHolder(View itemView) {
            super(itemView);
        }

        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){

        }

    }

    public class BannerModuleHoler extends BaseModuleHolder{
        public BannerModuleHoler(View itemView) {
            super(itemView);
            //设置banner图--比例4/6.0
            mBanner = itemView.findViewById(R.id.f_banner);
            int screenHeight = UIHelper.getScreenWidth(itemView.getContext());
            int height =  (int) (screenHeight* (4/6.0));
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(mBanner.getLayoutParams());
            params.height = height;
            mBanner.setLayoutParams(params);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            //设置图片集合
            ArrayList arrayList = new ArrayList<>();
            arrayList.add("http://chuantu.biz/t6/328/1528949888x-1376440114.jpg");//放入一张假图片
            arrayList.add("http://chuantu.biz/t6/328/1528949988x-1376440078.jpg");
            arrayList.add("http://chuantu.biz/t6/328/1528950013x-1376440078.png");
            mBanner.setImageLoader(new GlideImageLoader());
            mBanner.setImages(arrayList);
            //设置自动轮播，默认为true
//        banner.isAutoPlay(true);
            //设置轮播时间
//        banner.setDelayTime(1500);
            mBanner.start();
        }
    }


    public class  TitleModuleHolder extends BaseModuleHolder{

        private TextView textView;
        public TitleModuleHolder(View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.tv_conference_title);
        }

        //重新父类方法
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            //设置标题
            ItemModule model = modules.get(position);
            textView.setText(model.title);
            Log.d(TAG, "onBindViewHolder: ");

        }
    }



    public class  LiveModuleHolder extends BaseModuleHolder{

        private RecyclerView conferenceList;
        public LiveModuleHolder(View itemView) {
            super(itemView);
            conferenceList = itemView.findViewById(R.id.rv_conference_one);
        }

        //重新父类方法
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            ItemModule model = modules.get(position);
            if (model.modeuleType == ItemModuleType.ConfereneItem){
                //设置会议列表为横屏 和内部数据
//                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
//                        StaggeredGridLayoutManager.HORIZONTAL);
                LinearLayoutManager manager = new LinearLayoutManager(mContext);
                manager.setOrientation(LinearLayoutManager.HORIZONTAL);
                conferenceList.setLayoutManager(manager);
                MidModuleAdapter adapter = new MidModuleAdapter(model.modeuleType,model.models);
                conferenceList.setAdapter(adapter);
            }else if (model.modeuleType == ItemModuleType.LiveItem){
                //直播列表
                LinearLayoutManager manager = new LinearLayoutManager(mContext);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                conferenceList.setLayoutManager(manager);
                MidModuleAdapter adapter = new MidModuleAdapter(model.modeuleType,model.models);
                conferenceList.setAdapter(adapter);
            }

        }
    }
}
