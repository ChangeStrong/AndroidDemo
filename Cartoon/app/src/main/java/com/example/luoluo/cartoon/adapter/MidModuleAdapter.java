package com.example.luoluo.cartoon.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.luoluo.cartoon.R;
import com.example.luoluo.cartoon.UI.Home.model.ItemModel;
import com.example.luoluo.cartoon.UI.Home.model.ItemModule;
import com.example.luoluo.cartoon.Utils.Type.ItemModuleType;

import java.util.List;

public class MidModuleAdapter extends RecyclerView.Adapter {

    private static final String TAG = "MidModuleAdapter";
    List<ItemModel> mModels;
    ItemModuleType mModuleType;
    public  MidModuleAdapter(ItemModuleType type, List<ItemModel> models){
        mModels = models;
        mModuleType = type;
    }
    @Override
    public int getItemCount() {
        return mModels.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (mModuleType == ItemModuleType.ConfereneItem){
            //会议列表
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_mid_item,
                    parent,false);
            //屏幕的宽
            WindowManager manager =  (WindowManager)parent.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            int screenWidth = outMetrics.widthPixels;
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(view.getLayoutParams());
            params.width = screenWidth/2;
            view.setLayoutParams(params);
            return  new ConferencerHoler(parent.getContext(),view);
        }else {
            //直播列表
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item,
                    parent,false);
            return new LiveItemHoler(parent.getContext(),view);

        }


    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //刷新界面
        BaseHoler holder1 = (BaseHoler) holder;
        holder1.onBindViewHolder(holder,position);
    }

    public class   BaseHoler extends RecyclerView.ViewHolder{
        public BaseHoler(View itemView) {
            super(itemView);
        }
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {};

    }



    public class   ConferencerHoler extends BaseHoler{
        private ImageView imageView;
        private TextView textView;

        public ConferencerHoler(Context context, View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.iv_conference_bg);
            textView = itemView.findViewById(R.id.tv_conference_title);
            //屏幕的宽
            WindowManager manager =  (WindowManager)context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            int screenWidth = outMetrics.widthPixels;

            ViewGroup.LayoutParams params =   imageView.getLayoutParams();
            params.width = screenWidth/2;
            params.height = (int) (params.width*(9.0/16.0));
            imageView.setLayoutParams(params);

        }

        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {



            ItemModel model = mModels.get(position);
            imageView.setImageResource(model.imageId);
            textView.setText(model.title);
        }
    }

    public class LiveItemHoler extends BaseHoler{
        ImageView imageView;
        TextView textViewOne;
        TextView textViewTwo;
        TextView textViewThree;
        private  Context mContext;
        public LiveItemHoler(Context context, View itemView) {
            super(itemView);
            mContext = context;
            imageView = itemView.findViewById(R.id.iv_liveitem);
            textViewOne = itemView.findViewById(R.id.tv_liveitem_one);
            textViewTwo = itemView.findViewById(R.id.tv_liveitem_two);
            textViewThree = itemView.findViewById(R.id.tv_liveitem_three);

            //屏幕的宽
            WindowManager manager =  (WindowManager)context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            int screenWidth = outMetrics.widthPixels;

            ViewGroup.LayoutParams params =   imageView.getLayoutParams();
            params.width = screenWidth/2;
            params.height = (int) (params.width*(9.0/16.0));
        }

        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemModel model = mModels.get(position);
//            if (model.getClassImage() != null){
//                Glide.with(mContext).load(model.getClassImage())
//                        .placeholder(R.drawable.home_conference_item_bg)
//                        .into(imageView);
//            }else {
//
//            }
            textViewOne.setText("哈哈");
            textViewTwo.setText("666");
            textViewThree.setText("666");
//            Log.d(TAG, "onBindViewHolder: imageurl="+model.getClassImage());
        }
    }
}
