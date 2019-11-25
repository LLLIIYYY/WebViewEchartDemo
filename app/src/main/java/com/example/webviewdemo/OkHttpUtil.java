package com.example.webviewdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 单例非静态
 */

public class OkHttpUtil {

    private OkHttpClient client = new OkHttpClient();
    private Context context;
    private String url = "http://192.168.0.253:8080/api/v2/%s";
    private final Handler handler = new Handler();
    private ProgressDialog dialog;


    public OkHttpUtil(Context context) {
        this.context = context;
    }



    public void post(List<Map<String, String>> params, final CallBack callBack) {
        if (context != null) {
            dialog = new ProgressDialog(context);
            dialog.setMessage("正在进行网络请求...");
            dialog.show();
        }
        final String[] resultList = new String[params.size()];//results:结果数组，用于存储批量请求的结果
        final List<Integer> check = new ArrayList<>();//check:验证数组，用于验证批量请求是否都返回了结果
        for (int i = 0; i < params.size(); i++) {
            final int finalI = i;//此行代码不用写，会自动生成
            Map<String, String> map = params.get(i);
            for (String key : map.keySet()) {
                send(key, map.get(key), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (context != null) {
                            dialog.dismiss();
                        }
                        Log.e("HttpUtil", "onFailure: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        resultList[finalI] = response.body().string();
                        check.add(finalI);
                        if (check.size() == resultList.length) {
                            if (context != null) {
                                dialog.dismiss();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callBack.onFinish(resultList);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }


    public OkHttpUtil() {

    }

    public interface CallBack {//请求回传接口,单次多次请求都返回

        void onFinish(String[] resultList) throws Exception;
    }

    private static final String TAG = "OkHttpUtil";
    private void send(String action, String params, Callback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;"), params);
        Request request = new Request.Builder()
                .url(String.format(url, action))
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

}
