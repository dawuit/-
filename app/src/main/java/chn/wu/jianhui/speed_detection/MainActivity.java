package chn.wu.jianhui.speed_detection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import chn.wu.jianhui.speed_detection.utils.SqlUtils;
import chn.wu.jianhui.speed_detection.view.SpeedView;
/*
* @author W.J.H
* @email jianhui.wu.chn@hotmail.com
* @create at 2018/10/27
*/
public class MainActivity extends AppCompatActivity implements Button.OnClickListener, Switch.OnCheckedChangeListener, AbsListView.OnScrollListener
{


    private static final String CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING";


    private ListView listView;
    private SpeedView speedView;
    private ProgressBar progressBar;
    private TextView status_t;
    private TextView time_t;
    private TextView distance_t;
    private Button add_btn;
    private Button history_btn;
    private Button map_btn;
    private Switch save_switch;

    private MyBaseAdapter mAdapter = new MyBaseAdapter();
    private int[] item_icon = new int[]{R.drawable.start, R.drawable.highspeed, R.drawable.avgspeed, R.drawable.high, R.drawable.accuracy, R.drawable.site, R.drawable.direction};
    private String[] item_name = new String[]{"开始时间", "最高速度", "平均速度", "海拔高度", "位置精度", "位置信息", "当前方向"};
    private String[] item_content = new String[]{"--:--:--", "0.00 KM/H", "0.00 KM/H", "0 M", "0 M", "00.0000° 00.0000°",  "0.00°"};
    private List<LatLng> trackData = new ArrayList<>();
    private List<TraceLocation> traceLocations = new ArrayList<>();
    private double distance;
    private double max_speed;
    private Date start_time = new Date();
    private double avg_speed;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private DecimalFormat decimalFormat = new DecimalFormat("##0.00");
    private DecimalFormat decimalFormat_direction = new DecimalFormat("##0.00000");
    private int scrolledY;
    /*************************定位服务********************************/
    //定位服务类。此类提供单次定位、持续定位、地理围栏、最后位置相关功能
    //声明mlocationClient对象
    private AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption = null;

    //位置变化监听器
    private AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {

            if (aMapLocation != null)
            {
                if (aMapLocation.getErrorCode() == 0)
                {
                    traceLocations.add(new TraceLocation(aMapLocation.getLatitude(), aMapLocation.getLongitude(), aMapLocation.getSpeed(), aMapLocation.getBearing(), aMapLocation.getTime()));
                    double speed = aMapLocation.getSpeed() * 60 * 60 / 1000 ; // m/s 2 km/s
                    max_speed = max_speed < speed ? speed : max_speed;
                    avg_speed = distance / ((System.currentTimeMillis() - start_time.getTime())/1000.0/60/60);
                    item_content[0] = simpleDateFormat.format(start_time);
                    item_content[1] = decimalFormat.format(max_speed) + " KM/H";
                    item_content[2] = decimalFormat.format(avg_speed) + " KM/H";
                    item_content[3] = aMapLocation.getAltitude() + " M";
                    item_content[4] = aMapLocation.getAccuracy() + " M";
                    item_content[5] = decimalFormat_direction.format(aMapLocation.getLatitude()) + "°  " + decimalFormat_direction.format(aMapLocation.getLongitude()) + "°";
                    item_content[6] = decimalFormat.format(aMapLocation.getBearing()) + "°";
                    mAdapter.notifyDataSetChanged();
                    listView.invalidate();
                    listView.scrollTo(0, scrolledY);
                    time_t.setText(simpleDateFormat.format(new Date(System.currentTimeMillis() - start_time.getTime() - 8*60*60*1000)));
                    distance_t.setText(decimalFormat.format(distance) + " KM");
                    progressBar.setProgress(aMapLocation.getSatellites());
                    speedView.setStatues(speed, aMapLocation.getBearing());
                    status_t.setText("运行中");
                }
                else
                {
                    SignalBadView();
                    Toast.makeText(MainActivity.this,"AmapError"
                            + "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo(), Toast.LENGTH_LONG).show();
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    /*************************轨迹服务********************************/
    //服务key
    private LBSTraceClient lbsTraceClient;
    //轨迹纠编周期10秒
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            lbsTraceClient.queryProcessedTrace(1, traceLocations, LBSTraceClient.TYPE_AMAP, traceListener);
        }
    };
    private TraceListener traceListener = new TraceListener() {
        @Override
        public void onRequestFailed(int i, String s) {
            Log.v("gps", "轨迹纠编失败");
        }

        @Override
        public void onTraceProcessing(int i, int i1, List<LatLng> list) {

        }

        @Override
        public void onFinished(int i, List<LatLng> list, int i1, int i2) {
            trackData = list;
            distance = i1 / 1000.0;
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        /********************************定位服务**************************************/
        mlocationClient = new AMapLocationClient(getApplicationContext());
        mlocationClient.enableBackgroundLocation(2, createNotification());
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(aMapLocationListener);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();

        lbsTraceClient = LBSTraceClient.getInstance(getApplicationContext());

        listView.setAdapter(mAdapter);
        //启动轨迹纠编
        timer.schedule(timerTask, 10000, 10000);
    }



    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        lbsTraceClient.stopTrace();
        lbsTraceClient.destroy();
        mlocationClient.stopLocation();
        mlocationClient.onDestroy();
        //判断是否保存轨迹数据
        if (save_switch.isChecked())
        {
            new SqlUtils(getApplicationContext()).insertTrack(JSON.toJSONString(trackData), start_time, new Date());
        }
    }

    //设置信号差的视图
    private void SignalBadView()
    {
        status_t.setText("定位信号差");
        progressBar.setProgress(0);
        speedView.setStatues(0, 0);
    }

    //初始化界面
    private void initView()
    {
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        listView = findViewById(R.id.listview);
        status_t = findViewById(R.id.status);
        speedView = findViewById(R.id.speed_view);
        progressBar = findViewById(R.id.gpsProgressBar);
        distance_t = findViewById(R.id.distance);
        time_t = findViewById(R.id.time);
        add_btn = findViewById(R.id.addbtn);
        add_btn.setOnClickListener(this);
        add_btn.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/iconfont.ttf"));
        map_btn = findViewById(R.id.mapbtn);
        map_btn.setVisibility(View.GONE);
        map_btn.setOnClickListener(this);
        history_btn = findViewById(R.id.historybtn);
        history_btn.setVisibility(View.GONE);
        history_btn.setOnClickListener(this);
        save_switch = findViewById(R.id.saveswitch);
        save_switch.setOnCheckedChangeListener(this);
        listView.setOnScrollListener(this);
    }

    /**
     * 由于猎鹰sdk在运行时需要频繁进行定位，而从Android 8.0开始系统为实现降低功耗，对后台应用获取用户位置信息频率进行了限制，每小时只允许更新几次位置信息，详细信息请参考官方说明。按照官方指引，如果要提高位置更新频率，需要后台应用提供一个前台服务通知告知。
     * 猎鹰sdk提供了一个接口，只需调用该接口即可在轨迹上报服务启动时为您的应用创建一个前台服务通知，轨迹上报服务停止后，该通知会自动消失。这样，当您的应用切换到后台后仍然有一个前台服务通知存在，以此规避Android8.0对后台定位的限制。
     * @return
     */
    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID_SERVICE_RUNNING);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        builder.setContentIntent(PendingIntent.getActivity(MainActivity.this, 0, nfIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("行车辅助")
                .setContentText("车辅助运行中");
        Notification notification = builder.build();
        return notification;
    }

    /*
    * 重写返回键，实现确认退出，询问是否保存轨迹数据
    * @author W.J.H
    * @email jianhui.wu.chn@hotmail.com
    * @create at 2018/11/1
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            new AlertDialog.Builder(this).setTitle("提示").setMessage("您确定退出吗？").setPositiveButton("确定", (i, d)->{
                MainActivity.this.finish();
            }).setNegativeButton("取消", null).create().show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.addbtn:
                map_btn.setVisibility(map_btn.getVisibility() == View.GONE? View.VISIBLE : View.GONE);
                history_btn.setVisibility(history_btn.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                save_switch.setVisibility(save_switch.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                break;
            case R.id.mapbtn:
                Intent intentmap = new Intent(this, MapActivity.class);
                intentmap.putExtra("trackData", JSON.toJSONString(trackData));
                intentmap.putExtra("start_time", start_time);
                intentmap.putExtra("end_time", new Date());
                startActivity(intentmap);
                break;
            case R.id.historybtn:
                Intent intenttrack = new Intent(this, TrackListActivity.class);
                startActivity(intenttrack);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId())
        {
            case R.id.saveswitch:
                if (isChecked)
                    Toast.makeText(this, "保存轨迹 开启", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "保存轨迹 关闭", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class MyBaseAdapter extends BaseAdapter {
        //得到item的总数
        @Override
        public int getCount() {
            //返回ListView Item条目的总数
            return item_name.length;
        }
        //得到Item代表的对象
        @Override
        public Object getItem(int position) {
            //返回ListView Item条目代表的对象
            return item_name[position];
        }
        //得到Item的id
        @Override
        public long getItemId(int position) {
            //返回ListView Item的id
            return position;
        }
        //得到Item的View视图
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.info_viewitem,parent,false);
                holder = new ViewHolder();
                holder.icon = (ImageView)convertView.findViewById(R.id.icon);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.icon.setBackgroundResource(item_icon[position]);
            holder.name.setText(item_name[position]);
            holder.content.setText(item_content[position]);
            return convertView;
        }

        class ViewHolder
        {
            private ImageView icon;
            private TextView name;
            private TextView content;
        }
    }

    //保持滚动位置，更新数据后重新设置位置
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 不滚动时保存当前滚动到的位置
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (view != null) {
                scrolledY = listView.getScrollY();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
