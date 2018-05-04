package com.example.luoluo.interractivedemo.Util;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luoluo.interractivedemo.Model.LiveModel;
import com.example.luoluo.interractivedemo.R;

import java.util.ArrayList;
import java.util.List;

public class LiveListRecyclerAdapter extends RecyclerView.Adapter {

    public  List<?>mList;

    public  LiveListRecyclerAdapter(ArrayList list){
        mList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_liveitem,parent,false);

        ViewHoder hoder = new ViewHoder(view);

        return hoder;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LiveModel model = (LiveModel) mList.get(position);
        ViewHoder holder2 = (ViewHoder) holder;
        holder2.iamgeview.setImageResource(model.imageID);
        holder2.textView.setText(model.titleName);
    }

    public class ViewHoder extends RecyclerView.ViewHolder {
        public ImageView iamgeview;
        public TextView textView;

        public ViewHoder(View view) {
            super(view);

            iamgeview = view.findViewById(R.id.imageView);
            textView = view.findViewById(R.id.textView);
        }
    }
}
