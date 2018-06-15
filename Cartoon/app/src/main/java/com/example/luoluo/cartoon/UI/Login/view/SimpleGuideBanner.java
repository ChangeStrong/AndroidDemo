package com.example.luoluo.cartoon.UI.Login.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luoluo.cartoon.LLApplication;
import com.example.luoluo.cartoon.R;
import com.example.luoluo.cartoon.UI.Login.model.GuideData;
import com.example.luoluo.cartoon.Utils.UIHelper;
import com.flyco.banner.widget.Banner.BaseIndicatorBanner;


/**
 * Created by Seal on 2017/4/6.
 */

public class SimpleGuideBanner extends BaseIndicatorBanner<GuideData,SimpleGuideBanner> {
    public SimpleGuideBanner(Context context) {
        super(context,null,0);
        setBarShowWhenLast(false);
    }

    public SimpleGuideBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBarShowWhenLast(false);
    }

    public SimpleGuideBanner(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        setBarShowWhenLast(false);
    }

    @Override
    public View onCreateItemView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_guide_item, null,false);
        ImageView iv = (ImageView) view.findViewById(R.id.iv);
        TextView bt = (TextView) view.findViewById(R.id.bt);

        int screenWidth = UIHelper.getScreenWidth(LLApplication.getContext());
        ViewGroup.LayoutParams layoutParams = bt.getLayoutParams();
        layoutParams.width = screenWidth / 2;
        bt.setLayoutParams(layoutParams);

        GuideData guideData = mDatas.get(position);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        //设置图片
        iv.setImageResource(guideData.getBgRes());
        //判断图片显示还是隐藏
        bt.setVisibility(guideData.isShowEnter()? View.VISIBLE: View.GONE);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){

                    listener.onEnterButtonClick(v);
                }
            }
        });
        return view;
    }

    private OnEnterButtonClickListener listener;

    public void setListener(OnEnterButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnEnterButtonClickListener{
        void onEnterButtonClick(View v);

    }
}
