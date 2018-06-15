package com.example.luoluo.cartoon.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

public class UIHelper {
    /* dp转换成px
     */
    public static int dpTopx(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    /**
     * px转换成dp
     */
    public static int pxTodp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }

    //获取控件的尺寸---目前测试仅对xml中wrap_content有效---会使此控件走一次onMesure()方法
    public static Size getWidgetSize(View view){

        //通过先执行一次测量方法 获取控件的宽高
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);//开始测量
        int  height =view.getMeasuredHeight();
        int width =view.getMeasuredWidth();

        Size size  = new Size(width,height);
        return size;
    }

    //设置控件的宽
    public  static  void  setWidth(View view,int width){
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(view.getLayoutParams());
        params.width = width;
        view.setLayoutParams(params);
    }

    //设置控件的高
    public  static  void  setHeight(View view,int height){
        //此处的ViewGroup可替换  目前理解是为它的父布局使用什么就用什么设置
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(view.getLayoutParams());
        params.height = height;
        view.setLayoutParams(params);
    }


    //获取屏幕像素的宽
    public static int getScreenWidth(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;//获取的为像素
        return width;
    }
    //获取屏幕的高
    public  static  int getScreenHeight(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        return height;
    }

    public static double  angleToRadians(double angle){
        return angle*Math.PI/180;
    }

    /*
    //监听测量方法走完后获取尺寸
    ViewTreeObserver vto2 = mMenuImageButton.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mMenuImageButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            int[] startLoc = new int[2];
            mMenuImageButton.getLocationInWindow(startLoc);//此方法获取的高加上导航条了
            //mMenuImageButton.getMeasuredWidth()
            //mMenuImageButton.getMeasuredHeight()
            //mMenuImageButton.getLeft()
            //mMenuImageButton.getRight()
            //mMenuImageButton.getTop()
            //mMenuImageButton.getBottom()
        }
    });*/

   //相关类
    //Point

    //在活动走了 onCreate onStart onResume 方法走之后才会去走onMesure方法

    //************************其它设置**********
    /**
     * @return void 返回类型
     * @throws
     * @Title: setNoTitle_FullScreen
     * @Description: 设置全屏和没有标题
     */
    public static void setNoTitle_FullScreen(Activity activity, boolean isFull,
                                             boolean isNoTitle) {
        if (isFull)
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (isNoTitle)
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


}
