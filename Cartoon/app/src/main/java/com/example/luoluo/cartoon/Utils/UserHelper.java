package com.example.luoluo.cartoon.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.luoluo.cartoon.Utils.Constants.CONSTANTS;

public class UserHelper extends Object {

    //利用沙盒判断是否是第一次启动
    public static  boolean isFirstUse(Context context){
        return context.getSharedPreferences("cartoon",0).getBoolean("isFirstUse",true);
    }

    public static  void  setFirstUsed(Context context ,boolean isFirstUse){
        context.getSharedPreferences("cartoon",0).edit().putBoolean("isFirstUse",isFirstUse).commit();
    }

    /**
     * 判断之前是否有登录过
     *
     * @return
     */
    public static boolean isLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CONSTANTS.USERINFO,
                Context.MODE_PRIVATE);
        String userName = sp.getString("username", "");
        String password = sp.getString("password", "");
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            return true;
        }
        return false;
    }


}
