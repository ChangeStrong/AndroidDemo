package com.example.luoluo.interractivedemo.Util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.example.luoluo.interractivedemo.LLApplication;

public class UIHelper{


    public static int getScreenWidth(){
                LLApplication application = (LLApplication) LLApplication.getContext();

        WindowManager manager =  (WindowManager)application
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        return width;
    }




    public  static int getScreenHeight(){
        LLApplication application = (LLApplication) LLApplication.getContext();
        WindowManager manager =  (WindowManager)application
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        return height;
    }
}
