package com.example.webviewdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.abel533.echarts.AxisPointer;
import com.github.abel533.echarts.DataZoom;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Magic;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.feature.MagicType;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Line;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                ArrayList<Map<String, String>> request = new ArrayList<>();
                HashMap<String, String> requestHM = new HashMap<>();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("UserName", "user1");
                requestHM.put("get_all_sense", jsonObject.toString());
                request.add(requestHM);
                okHttpUtil.post(request, new OkHttpUtil.CallBack() {
                    @Override
                    public void onFinish(String[] responses) throws Exception {
                        for (int i = 0; i < responses.length; i++) {
                            JSONObject jsonObject1 = new JSONObject(responses[i]);
                            if (!"S".equals(jsonObject1.getString("RESULT"))) {
                                Toast.makeText(MainActivity.this, jsonObject1.getString("ERRMSG"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        EnvBean envBean = gson.fromJson(responses[0], EnvBean.class);
                        if (data[0].size() > 5) {
                            for (int i = 0; i < data.length; i++) {
                                data[i].removeFirst();
                            }
                            xVals.removeFirst();
                            xVals.add(simpleDateFormat.format(System.currentTimeMillis()));
                        }
                        data[0].add(envBean.getTemperature());
                        data[1].add(envBean.getHumidity());
                        data[2].add(envBean.getLightIntensity());
                        data[3].add(envBean.get_$Pm25120());
                        data[4].add(envBean.getCo2());
                        updateInfo();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.sendEmptyMessageDelayed(0, 3000);
            return false;
        }
    });
    private String[] triggerType;

    private void updateInfo() {
        for (int i = 0; i < webViews.length; i++) {
//            GsonOption gsonOption = new GsonOption();
//            gsonOption.yAxis(new ValueAxis());
//            gsonOption.xAxis(new CategoryAxis().data(xVals.toArray()));
//            Line line = new Line();
//            line.data(data[i].toArray());
//            line.name(titles[i]);
//            line.smooth(true);
//            line.tooltip().setTrigger(Trigger.item);
//            line.tooltip().axisPointer(new AxisPointer().type(PointerType.cross));
//            gsonOption.toolbox().show(true).feature(new DataZoom(), new MagicType(Magic.bar, Magic.line));
//            gsonOption.series(line);
//            gsonOption.legend().show(true);
//            Log.e(TAG, "updateInfo: " + gsonOption.toString());
//            webViews[i].loadUrl("javascript:updateInfo(" + gsonOption.toString() + ")");
            String option = "{\n" +
                    "            series:{\n" +
                    "                data:"+data[i].toString()+"\n" +
                    "            },\n" +
                    "            xAxis:{\n" +
                    "                data:"+xVals.toString()+"\n" +
                    "            }\n" +
                    "        }";
            webViews[i].evaluateJavascript("javascript:updateInfo(" + option + ", "+i+")", null);
        }
    }

    private static final String TAG = "MainActivity";
    private Gson gson;
    private OkHttpUtil okHttpUtil;
    private LinkedList<Integer>[] data;
    private LinkedList<String> xVals;
    private SimpleDateFormat simpleDateFormat;
    private String[] chartType;
    private ViewPager vp;
    private RadioGroup rg;
    private WebView[] webViews;
    private String[] titles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        okHttpUtil = new OkHttpUtil();
        gson = new Gson();
        initView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "SimpleDateFormat"})
    private void initView() {
        vp = (ViewPager) findViewById(R.id.vp);
        rg = (RadioGroup) findViewById(R.id.rg);
        ((RadioButton) rg.getChildAt(0)).setChecked(true);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) rg.getChildAt(position)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                vp.setCurrentItem(group.indexOfChild(group.findViewById(group.getCheckedRadioButtonId())));
            }
        });

        webViews = new WebView[]{new WebView(MainActivity.this), new WebView(MainActivity.this),
                new WebView(MainActivity.this), new WebView(MainActivity.this),
                new WebView(MainActivity.this)};

        for (int i = 0; i < webViews.length; i++) {
            WebView webView = webViews[i];
            webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("file:///android_asset/h/echart.html", new HashMap<String, String>());
        }

        webViews[webViews.length - 1].setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                handler.sendEmptyMessage(0);
                for (int i = 0; i < webViews.length; i++) {
                    final int finalI = i;
                    webViews[i].setWebChromeClient(new WebChromeClient(){
                        @Override
                        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                            Log.e(TAG, "onConsoleMessage: "+ finalI +"\t"+consoleMessage.message());
                            return super.onConsoleMessage(consoleMessage);
                        }

                        @Override
                        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                            return super.onJsAlert(view, url, message, result);
                        }
                    });
                    webViews[i].evaluateJavascript("javascript:initChart("+i+")", null);
                }
                super.onPageFinished(view, url);
            }
        });

        vp.setOffscreenPageLimit(webViews.length - 1);
        vp.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return webViews.length;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                WebView webView = webViews[position];
                linearLayout.addView(webView);
                container.addView(linearLayout);
                return linearLayout;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });


        chartType = new String[]{"line", "bar", "pie", "line", "line"};
        triggerType = new String[]{"axis", "axis", "item", "axis", "axis"};
        titles = new String[]{"温度", "湿度", "光照", "PM25", "Co2"};
        data = new LinkedList[]{new LinkedList<Integer>(), new LinkedList<Integer>(), new LinkedList<Integer>(), new LinkedList<Integer>(), new LinkedList<Integer>()};
        xVals = new LinkedList<>();
        simpleDateFormat = new SimpleDateFormat("ss");
        for (int i = 0; i < 6; i++) {
            xVals.add(simpleDateFormat.format(System.currentTimeMillis() + i * 3000));
        }

    }

}
