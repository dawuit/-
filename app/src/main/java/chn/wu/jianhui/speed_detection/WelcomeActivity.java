package chn.wu.jianhui.speed_detection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.widget.Toast;
import chn.wu.jianhui.speed_detection.utils.GPS_Interface;
import chn.wu.jianhui.speed_detection.utils.GPS_Presenter;

public class WelcomeActivity extends Activity implements GPS_Interface
{
    private GPS_Presenter gps_presenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //获取授权
        getPermissions();
    }


    //获取权限
    protected void getPermissions()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET
            };
            int i;
            for(i = 0; i<permissions.length; ++i)
            {
                if(checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED)
                    break;
            }
            if(i != permissions.length)
            {
                requestPermissions(permissions, 0);
                return;
            }

        }
        openGps();
    }

    //权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 0)
        {
            //判断权限是否允许
            if (grantResults[0] == -1 || grantResults[1] == -1 || grantResults[2] == -1 || grantResults[3] == -1)
            {
                new AlertDialog.Builder(this).setTitle("提示").setMessage("请授权权限")
                        .setPositiveButton("确定",(d, i)->{
                            getPermissions();
                        }).setNegativeButton("取消", (d, i) ->{
                            this.finish();
                        }).show();
            }
            else
            {
                openGps();
            }
        }
    }

    //要求打开gps
    //判断有没有打开gps，没有打开则注册监听，有则进入应用
    protected void openGps()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(WelcomeActivity.this, "请打开GPS", Toast.LENGTH_LONG).show();
            Intent intent=new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            gps_presenter = new GPS_Presenter( WelcomeActivity.this , WelcomeActivity.this );
        }
        else
        {
            gpsSwitchState(true);
        }
    }


    @Override
    public void gpsSwitchState(boolean gpsOpen) {
        if (gpsOpen)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        if ( gps_presenter != null ){
            gps_presenter.onDestroy();
        }
    }
}
