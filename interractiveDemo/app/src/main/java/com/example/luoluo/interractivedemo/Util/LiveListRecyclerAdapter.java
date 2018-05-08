package com.example.luoluo.interractivedemo.Util;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luoluo.interractivedemo.Enum.LiveItemType;
import com.example.luoluo.interractivedemo.Model.LiveModel;
import com.example.luoluo.interractivedemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiveListRecyclerAdapter extends RecyclerView.Adapter {

    public static final String TAG = "LiveListRecyclerAdapter";
    private static  final  int TYPE_Title = 0;
    private static  final int TYPE_First = 1;
    private static  final int TYPE_Second = 2;
    public  List<LiveModel>mList;

    public  LiveListRecyclerAdapter(ArrayList list){
        mList = list;
    }

    //返回当前cell类型
    @Override
    public int getItemViewType(int position) {
        LiveModel model = mList.get(position);
        switch (model.type) {
            case LiveItemTypeTitle:
                return TYPE_Title;
            case LiveItemTypeFirst:
                return TYPE_First;
            case LiveItemTypeTitleSecond:
                return TYPE_Second;
            default:
                return TYPE_Second;

        }
    }

    //返回当前cell使用的xml
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        BaseViewHoder hoder;
        switch (viewType) {
            case TYPE_Title:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .recycler_liveitem_title, parent, false);
                hoder = new ViewHoderTitle(view);
                hoder.setType(LiveItemType.LiveItemTypeTitle);
                break ;

            case TYPE_First:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                                .recycler_liveitem_first, parent,
                        false);
                hoder = new  ViewHoderFirst(view);
                hoder.setType(LiveItemType.LiveItemTypeFirst);
            break;
            case TYPE_Second:

            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                                .recycler_liveitem, parent,
                        false);
                hoder = new ViewHoder(view);
                hoder.setType(LiveItemType.LiveItemTypeTitleSecond);
                break;
        }



        return hoder;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //刷新UI内容
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BaseViewHoder) {
            LiveModel model = (LiveModel) mList.get(position);
            BaseViewHoder baseViewHoder = (BaseViewHoder) holder;
            Log.d("onBindViewHolder", "onBindViewHolder: "+baseViewHoder.getType());
            switch (baseViewHoder.getType()) {
                case LiveItemTypeTitle:
                    ViewHoderTitle titleHoder = (ViewHoderTitle) holder;
                    titleHoder.textView.setText(model.titleNames[0]);
                    break;
                case LiveItemTypeFirst:
                    ViewHoderFirst viewHoderFirst = (ViewHoderFirst) holder;
                    viewHoderFirst.textViewOne.setText(model.titleNames[1]);
                    viewHoderFirst.textViewTwo.setText(model.titleNames[2]);
                    viewHoderFirst.imageViewOne.setImageResource(model.imageIDS.get(0));
                    viewHoderFirst.imageViewTwo.setImageResource(model.imageIDS.get(1));
                    break;
                case LiveItemTypeTitleSecond:
                default:

                    Random random = new Random();
                    ViewHoder holder2 = (ViewHoder) holder;
                    holder2.iamgeview.setImageResource(model.imageIDS.get(random.nextInt(model.imageIDS.size()-1)));
                    holder2.textView.setText(model.titleNames[random.nextInt(model.titleNames.length-1)]);
                    break;

            }

        }
    }


    //父类
    public class  BaseViewHoder extends RecyclerView.ViewHolder{
        public LiveItemType type;
        public BaseViewHoder(View view){
            super(view);
        }
        public void setType(LiveItemType type){
            this.type = type;
        }
        public LiveItemType getType(){
            return this.type;
        }


    }
    public class ViewHoderTitle extends BaseViewHoder{
        public  TextView textView;
        public  ViewHoderTitle(View view){
            super(view);
            textView = view.findViewById(R.id.title_textview);

        }
    }

    public class ViewHoderFirst extends BaseViewHoder{
        public  ImageView imageViewOne;
        public ImageView imageViewTwo;
        public TextView textViewOne;
        public TextView textViewTwo;

        public  ViewHoderFirst(View view){
            super(view);

            imageViewOne = view.findViewById(R.id.iv_first_one_one);
            imageViewTwo = view.findViewById(R.id.iv_first_two_one);
            textViewOne = view.findViewById(R.id.tv_first_one_one_title);
            textViewTwo = view.findViewById(R.id.tv_first_two_one_title);

        }
    }

    //第二种item的viewHoder
    public class ViewHoder extends BaseViewHoder {
        public ImageView iamgeview;
        public TextView textView;

        public ViewHoder(View view) {
            super(view);

            iamgeview = view.findViewById(R.id.imageView);
            textView = view.findViewById(R.id.textView);
        }
    }
}
