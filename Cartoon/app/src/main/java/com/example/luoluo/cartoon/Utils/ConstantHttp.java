package com.example.luoluo.cartoon.Utils;

public class ConstantHttp {
    public  static String getBaseUrl()
    {
        return "http://183.63.35.28:8883/";
    }
    /**
     * 校园直播信息集
     */
    public static String getClassLiveUrl() {
        return getBaseUrl() + "ios/live_show.jspx";

    }

}
