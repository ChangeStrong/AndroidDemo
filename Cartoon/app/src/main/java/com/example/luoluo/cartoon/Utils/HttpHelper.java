package com.example.luoluo.cartoon.Utils;

import android.util.Log;

import com.example.luoluo.cartoon.Utils.Constants.ConstantHttp;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpHelper {


    private static final String TAG = "HttpHelper";
    //回调接口
    public interface OnHttpListener{

        default void onGetLiveList(){

        }
    }

    private OnHttpListener liveListListener;
    //获取直播列表数据
    public void getLiveListData(OnHttpListener listener){
    this.liveListListener = listener;

        final JSONObject requestData = new JSONObject();
        try {
            requestData.put("type", "1");
        }catch (JSONException e){
            e.printStackTrace();
        }

        OkGo.<String>post(ConstantHttp.getClassLiveUrl())
                .headers("Content-Type", "application/json")
                .tag(this)
                .cacheKey("class_live_data")
                .cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)
                .upJson(requestData)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String data = response.body().toString();
                        Gson gson = new Gson();
//                        LiveListModel listModel = gson.fromJson(data,LiveListModel.class);

                       //回调出去
                        if (liveListListener != null){
                            liveListListener.onGetLiveList();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        Log.d(TAG, "onError= "+response.toString());
                        super.onError(response);

                    }
                });
    }



}
