package com.example.luoluo.animationdemo;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.luoluo.animationdemo.Utils.CommonUtil;

public class MainActivity extends AppCompatActivity {

    LLCustomView mCustomView;
public String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCustomView = findViewById(R.id.iv_keyAnimations);
        //获取屏幕像素的宽、高
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;//获取的为像素
        int height = outMetrics.heightPixels;

        Log.d(TAG, "onCreate: width="+width+"px"+" height="+height+"px");
        Log.d(TAG, "onCreate: widthdp= "+ CommonUtil.pxTodp(this,width)
                +"heightdp="+CommonUtil.pxTodp(this,height));
//      Log.d(TAG, "onCreate:200  width"+dp2px(this,200));

        /*
        ImageView imageView = findViewById(R.id.iv_keyAnimations);
        //帧动画
        imageView.setImageResource(R.drawable.frame_key_animat);//设置动态图片集
        AnimationDrawable animationDrawable1 = (AnimationDrawable) imageView.getDrawable();
        animationDrawable1.start();
        //属性动画---通过xml设置属性.
//        imageView.setImageResource(R.drawable.a_0);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.alpha_anim);
//        animation.setDuration(10);
        imageView.startAnimation(animation);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
    CommonUtil.getWidgetSize(mCustomView);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //监听测量方法走完后获取尺寸
        ViewTreeObserver vto2 = mCustomView.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCustomView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int[] startLoc = new int[2];
                mCustomView.getLocationInWindow(startLoc);//此方法获取的高加上导航条了
                Log.d(TAG, "onResume: "+ mCustomView.getMeasuredWidth()+" "+mCustomView.getMeasuredHeight());
                Log.d(TAG, "onResume: "+startLoc[0]+" "+startLoc[1]);
                System.out.println("图片各个角Left："+mCustomView.getLeft()
                        +" Right："+mCustomView.getRight()+" Top："
                        +mCustomView.getTop()+" Bottom："
                        +mCustomView.getBottom());
            }
        });
    }




}
