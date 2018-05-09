package com.example.luoluo.interractivedemo.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class BannerViewPager extends ViewPager  {
    public static String TAG = "BannerViewPager";
    public  boolean onTouchEventStatus = false;
    float startX = 0;
    float startY = 0;
    float lastX = 0;
    float lastY = 0;
    int initStartAndLast = 0;
    public BannerViewPager(@NonNull Context context) {
        super(context);
    }

    public BannerViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);//默认走的是这个构造函数

    }

    //接到一个事件需要响应
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        this.updateCoordin(ev);//更新并记录开始和结束坐标
        if (initStartAndLast <= 1){
            //未获取到起始和结束点  让它多走两个点用来初始化
            return super.dispatchTouchEvent(ev);
        }
        boolean dd = false;
        //是取消事件、且是左滑、这个事件后将会滑出bannerPager
        if (ev.getAction() == MotionEvent.ACTION_CANCEL && isLeftFlip() && willFlipOutPager()){
            this.onTouchEventStatus = true;//是取消事件 则父类也不用执行
            Log.d(TAG, "dispatchTouchEvent: "+dd + ev.getAction()+ "忽略此事件");
            return  false;//忽略这个事件
        }

        dd = super.dispatchTouchEvent(ev);
        Log.d(TAG, "dispatchTouchEvent: "+dd + ev.getAction());
        return dd;
    }

    //自己去消费这个事件
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //此处返回false则 对此次滑动不做处理  如果返回ture表示这次事件被我消费了 不用再传回去给上级消费了
//        ev.getRawX();ev.getRawY();//获取相对于屏幕的坐标
//        ev.getX();//获取相对于点击的View的坐标点
//        ev.getHistorySize();
//         ev.getHistoricalEventTime();

        Boolean result = false;
        result = super.onTouchEvent(ev);
        Log.d(TAG, "onTouchEvent: "+result);
        Log.d(TAG, "onTouchEvent: " + " Action="+ev.getAction()+" startX="+startX +" lastX="+lastX);
        return  result;

    }

    //返回ture 则可以打断后面的子view去响应 直接回传给上层(父视图)
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public  void  updateCoordin(MotionEvent ev){
        //记录滑动的坐标
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: //0
                //开始触摸
                startX = ev.getX();
                startY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:  //2
                //移动中
                lastX = ev.getX();
                lastY = ev.getY();
                break;

            case MotionEvent.ACTION_CANCEL: //触摸取消 3
                lastX = ev.getX();
                lastY = ev.getY();
                this.onTouchEventStatus = true;//是取消事件 则父类也不用执行
                Log.d(TAG, "recoderCoordin: "+"action cancel.");
                break;
            case MotionEvent.ACTION_UP://触摸结束  1
                lastX = ev.getX();
                lastY = ev.getY();
                break;

            default:break;
        }
    }


    //是否左滑
    public  boolean isLeftFlip(){
        return  lastX < startX;
    }
    public  boolean willFlipOutPager(){
        int willcurrentPage = this.getWillFlipPager();
        if (willcurrentPage == this.getChildCount()) {
            Log.d(TAG, "isLastPager: ");
            return  true;
        }
        return false;
    }


    //这次滑动事件后将滑动到的页数
    public  int getWillFlipPager(){
        int willcurrentPage = this.getCurrentItem();
        if (lastX < (startX -20)){
            //这次是左滑了
            willcurrentPage++;
        }else if (lastX > (startX+20))
        {
            //这次是右滑
            willcurrentPage--;
            if (willcurrentPage <=0){
                willcurrentPage= 0;
            }
        }
        return  willcurrentPage;
    }

}
