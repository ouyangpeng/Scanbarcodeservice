package com.scanbarcodeservice;

import android.content.Intent;
import android.os.Bundle;
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
