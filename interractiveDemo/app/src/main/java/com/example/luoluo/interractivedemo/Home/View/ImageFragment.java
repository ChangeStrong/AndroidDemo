package com.example.luoluo.interractivedemo.Home.View;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.luoluo.interractivedemo.R;
import com.example.luoluo.interractivedemo.Util.UIHelper;

public class ImageFragment extends Fragment {

    public  int imageSourceid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image,container,false);
        ImageView imageView = view.findViewById(R.id.fragment_image_imageView);
        if (this.imageSourceid >0){
            imageView.setImageResource(this.imageSourceid);
        }
        //重新设置宽高
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) imageView.getLayoutParams(); //取控件textView当前的布局参数 linearParams.height = 20;// 控件的高强制设成20
//        linearParams.width = UIHelper.getScreenWidth();// 控件的宽强制设成30
        linearParams.height = (int) (UIHelper.getScreenWidth()*(3.0/4.0));
        imageView.setLayoutParams(linearParams);
        return view;
    }
}
