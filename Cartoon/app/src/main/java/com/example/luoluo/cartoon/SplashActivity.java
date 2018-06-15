package com.example.luoluo.cartoon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.luoluo.cartoon.UI.Login.GuideActivity;
import com.example.luoluo.cartoon.UI.Login.PhoneLoginActivity;
import com.example.luoluo.cartoon.Utils.UIHelper;
import com.example.luoluo.cartoon.Utils.UserHelper;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置此活动全屏
        UIHelper.setNoTitle_FullScreen(this, true, true);
        setContentView(R.layout.activity_splash);
        //开始进入
        startEnter();

    }

    private void startEnter(){
        //判断是否首次进入App
        if (UserHelper.isFirstUse(this)) {
            //如果没登陆过，则进入引导页
            startActivity(new Intent(this, GuideActivity.class));
            Log.d(TAG, "startEnter: gudide Activity.");
        } else {
            //判断是否有登陆过App
            if (UserHelper.isLogin(this)) {
                //如果有登陆直接进入首页
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                //如果没有登陆过，进入登陆页
                startActivity(new Intent(SplashActivity.this, PhoneLoginActivity.class));
            }
        }
        finish();
    }
}
