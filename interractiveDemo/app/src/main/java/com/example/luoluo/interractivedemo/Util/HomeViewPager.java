package com.example.luoluo.interractivedemo.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.example.luoluo.interractivedemo.Home.View.HomeFragment;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class HomeViewPager extends ViewPager {
    public static String TAG = "HomeViewPager";
    float startX = 0;
    float lastX = 0;
    int initStartAndLast = 0;
    public HomeFragment homeFragment;
    public HomeViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //接到一个事件需要响应
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        this.updateCoordin(ev);//更新并记录开始和结束坐标
        if (initStartAndLast <= 1){
            //未获取到起始和结束点  让它多走两个点用来初始化
            return super.dispatchTouchEvent(ev);
        }
        if (!isLeftFlip()){
            homeFragment.mBannerPager.onTouchEventStatus = false;//本viewPager的事件响应
            Log.d(TAG, "dispatchTouchEvent: resume response chain.");
        }


        boolean result = false;
if (homeFragment.mBannerPager.onTouchEventStatus){
    Log.d(TAG, "dispatchTouchEvent: "+result);
    return result;
}
        result =super.dispatchTouchEvent(ev);//如果未调父类的这个方法则下面的onTouchEvent永远不会执行
        Log.d(TAG, "dispatchTouchEvent: "+result + ev.getAction());
        return result;
    }

    //自己去消费这个事件
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = false;
            result =super.onTouchEvent(ev);
        Log.d(TAG, "onTouchEvent: " + result);
        Log.d(TAG, "onTouchEvent: " + ev.getAction());
        return result;
    }


    //是否左滑
    public  boolean isLeftFlip(){
        Log.d(TAG, "isLeftFlip: "+"startX="+startX+"lastX="+lastX);
        return  lastX < startX;
    }

    public  void  updateCoordin(MotionEvent ev){
        //记录滑动的坐标
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: //0
                //开始触摸
                startX = ev.getX();
                initStartAndLast = 1;
                break;

            case MotionEvent.ACTION_MOVE:  //2
                //移动中
                lastX = ev.getX();
                initStartAndLast = 2;
                break;

            case MotionEvent.ACTION_CANCEL: //触摸取消 3
                lastX = ev.getX();
                initStartAndLast = 3;
                break;
            case MotionEvent.ACTION_UP://触摸结束  1
                lastX = ev.getX();
                initStartAndLast = 4;
                break;

            default:break;
        }
        Log.d(TAG, "updateCoordin: "+"startX="+startX+"lastX="+lastX
                +" action="+ev.getAction());
    }

}
