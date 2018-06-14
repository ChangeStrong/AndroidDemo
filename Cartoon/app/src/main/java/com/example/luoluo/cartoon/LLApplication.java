package com.example.luoluo.cartoon;

import android.app.Application;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

public class LLApplication extends Application{
    private static final String TAG = "LLApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化网络请求库
        initOkgo();
    }

    public void initOkgo(){
        //配置OkGo的全局参数
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);//添加OkGo默认debug日志
        //超时时间设置，默认60秒
        builder.readTimeout(10000, TimeUnit.SECONDS);      //全局的读取超时时间/MILLISECONDS/10000
        builder.writeTimeout(10000, TimeUnit.SECONDS);     //全局的写入超时时间/MILLISECONDS/OkGo.REFRESH_TIME
        builder.connectTimeout(10000, TimeUnit.SECONDS);   //全局的连接超时时间/MILLISECONDS/OkGo.REFRESH_TIME
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));

        OkGo.getInstance().init(this)
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
//                .addCommonHeaders(httpHeaders)                //设置全局公共头
                .setRetryCount(3);
        Log.d(TAG, "initOkgo: init Okgo library finished.");
    }
}
