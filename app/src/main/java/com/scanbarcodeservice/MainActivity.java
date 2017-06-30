package com.scanbarcodeservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    Button btnopen;
    Button btnclose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnopen = (Button) findViewById(R.id.btn_open);
        btnclose = (Button) findViewById(R.id.btn_close);
        acquireWakeLock();
        btnopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, ScanServices.class);
                        startService(intent);
                    }
                }).start();
            }
        });
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ScanServices.class);
                stopService(intent);
            }
        });
    }
    PowerManager.WakeLock wakeLock = null;
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }


    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_F5) {
            Intent intent = new Intent();
            intent.setAction("keycode.f4.down");
            this.sendOrderedBroadcast(intent, null);
        }

        return super.onKeyDown(keyCode, event);
    }
}
