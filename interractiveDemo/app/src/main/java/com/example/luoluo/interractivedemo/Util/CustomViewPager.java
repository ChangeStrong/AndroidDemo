package com.example.luoluo.interractivedemo.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager  {
    public static String TAG = "CustomViewPager";
    float startX = 0;
    float startY = 0;
    float lastX = 0;
    float lastY = 0;
    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);//默认走的是这个构造函数

    }

    //接到一个事件需要响应
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    //自己去消费这个事件
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //此处返回false则 对此次滑动不做处理  如果返回ture表示这次事件被我消费了 不用再传回去给上级消费了
//        ev.getRawX();ev.getRawY();//获取相对于屏幕的坐标
//        ev.getX();//获取相对于点击的View的坐标点
//        ev.getHistorySize();
//         ev.getHistoricalEventTime();

    Boolean isMove = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //开始触摸
                startX = ev.getX();
                startY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                //移动中
                isMove = true;
            break;

            case MotionEvent.ACTION_UP://触摸结束
                lastX = ev.getX();
                lastY = ev.getY();
            break;
            default:break;
        }

        Log.d(TAG, "onTouchEvent: x="+ev.getX()+"y="+ev.getY()+"position="+this.getCurrentItem()
                +"pages="+this.getChildCount());
        if ((this.getCurrentItem()+1) == this.getChildCount()-1){
            //已经处于最后一页了、判断是否扔需要滑动
            Log.d(TAG, "onTouchEvent: 已经处于最后一页了");
            if (lastX<startX){
                //左滑--已经最后一页了无需滑动了
                return true;//表示我已经处理了 不用给其它人处理了
            }else {
                return super.onTouchEvent(ev);
            }

        }else {
            return super.onTouchEvent(ev);
        }



    }

    //返回ture 则可以打断后面的子view去响应 直接回传给上层(父视图)
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
