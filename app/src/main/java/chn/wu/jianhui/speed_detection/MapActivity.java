package chn.wu.jianhui.speed_detection;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.TraceOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapActivity extends AppCompatActivity implements Button.OnClickListener
{
    private MapView mMapView = null;
    private List<LatLng> trackData = new ArrayList<>();
    private Date start_time;
    private Date end_time;
    private TraceOverlay traceOverlay;
    private Button back_btn;
    private TextView distance_t;
    private TextView map_start_time;
    private TextView map_time;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        back_btn = findViewById(R.id.backbtn);
        distance_t = findViewById(R.id.mapdistance);
        map_start_time = findViewById(R.id.map_start_time);
        map_time = findViewById(R.id.map_time);
        back_btn.setOnClickListener(this);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        drawTrack();
    }

    //获取轨迹数据
    protected void getTrackData()
    {
        Intent intent = getIntent();
        JSONArray jsonArray = JSON.parseArray(intent.getStringExtra("trackData"));
        JSONObject jsonObject = null;
        for(int i=0; i<jsonArray.size(); ++i)
        {
            jsonObject = jsonArray.getJSONObject(i);
            trackData.add(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
        }
        start_time = (Date) intent.getSerializableExtra("start_time");
        end_time = (Date) intent.getSerializableExtra("end_time");
    }

    //绘制轨迹,显示数据
    protected void drawTrack()
    {
        getTrackData();
        traceOverlay = new TraceOverlay(mMapView.getMap(), trackData);
        traceOverlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FINISH);
        traceOverlay.zoopToSpan();
        map_start_time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start_time));
        map_time.setText("持续时间："+new SimpleDateFormat("HH:mm:ss").format(new Date(end_time.compareTo(start_time)  - 8*60*60*1000)));
        distance_t.setText("轨迹纠编：" + traceOverlay.getDistance() + " KM");
        ((Button)findViewById(R.id.backbtn)).setTypeface(Typeface.createFromAsset(getAssets(),"fonts/iconfont.ttf"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
