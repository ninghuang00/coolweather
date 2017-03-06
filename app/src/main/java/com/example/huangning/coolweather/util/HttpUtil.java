package com.example.huangning.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by huangning on 2017/3/6.
 * 用于与服务器交互
 */

public class HttpUtil {
    //发送HTTP请求

    /**
     * @param address 请求地址
     * @param callback 注册回调来处理服务器响应
     */
    public void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
