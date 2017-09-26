package com.scanbarcodeservicetest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.scanbarcodeservicetest.utils.ProgressDialogUtils;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    Button btnopen;
    Button btnclose;
    Button btnSure; //确认图片文件数量上限
    Button btnDel; //删除文件夹内所有文件
    EditText etInput; //输入图片文件数量上限
    EditText etShow; //显示扫描结果
    TextView tvShow; //显示当前文件夹内图片数量
    int num = 0; //设置的图片数量上限

    //扫描生成的图片文件的个数
    public static int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnopen = (Button) findViewById(R.id.btn_open);
        btnclose = (Button) findViewById(R.id.btn_close);

        btnSure = (Button) findViewById(R.id.btn_sure);
        btnDel = (Button) findViewById(R.id.btn_del);

        etInput = (EditText) findViewById(R.id.et_input);
        etShow = (EditText) findViewById(R.id.et_show);
        tvShow = (TextView) findViewById(R.id.tv_show);


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

                //停止
                handler.removeCallbacksAndMessages(null);
            }
        });

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取设置的数量上限值
                final String top = etInput.getText().toString();
                if ("".equals(top)){

                    if(etShow.isFocused()){
                        //已获得焦点
                    }else{
                        etShow.requestFocus();//获得焦点
                    }
                    //开始实时显示文件夹内文件数量
                    handler.sendEmptyMessage(1);
                    sendBroadcast2("keycode.f4.down");

                    return;
                }
                if (!ProgressDialogUtils.isNumeric(top)||top.length() > 10){
                    etInput.setText("");
                    etInput.setHint("请输入正确的数量");
                    return;
                }
                final Integer topInt = Integer.parseInt(top);
                num = topInt;
                handler.sendEmptyMessage(2);
                //开始实时显示文件夹内文件数量
                handler.sendEmptyMessage(1);

                if(etShow.isFocused()){
                    //已获得焦点
                }else{
                    etShow.requestFocus();//获得焦点
                }
                sendBroadcast2("keycode.f4.down");

            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除文件
                delData();
            }
        });

        i = 0;
        String path = "/sdcard/Boohee/";
        getFiles(path);
        tvShow.setText(i + "");

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

    //获取图片文件个数
    private void getFiles(String string) {
        // TODO Auto-generated method stub
        File file = new File(string);
        if (!file.exists()) {
            file.mkdirs();

        }
        File[] files = file.listFiles();

        for (int j = 0; j < files.length; j++) {
            String name = files[j].getName();
            if (files[j].isDirectory()) {
                String dirPath = files[j].toString().toLowerCase();
                System.out.println(dirPath);
                getFiles(dirPath + "/");
            } else if (files[j].isFile() & name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
                i++;
            }
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                i = 0;
                String path = "/sdcard/Boohee/";
                getFiles(path);
                tvShow.setText(i + "");
                sendEmptyMessageDelayed(1,1000);
            } if (msg.what == 2) {

                if (i > num){
                    tvShow.setText("已达到图片数量上限");
                    sendBroadcast("com.setscan.continuous", false);
                    //停止
                    handler.removeCallbacksAndMessages(null);
                    initScanTime();
                }
                sendEmptyMessageDelayed(2,1000);
            } if (msg.what == 3) {
                ProgressDialogUtils.dismissProgressDialog();
                i = 0;
                tvShow.setText(0 + "");
                handler.removeCallbacksAndMessages(null);
            }

        }
    };
    //发送广播修改连续扫描状态
    private void sendBroadcast(String stirng, boolean b) {
        Intent intent = new Intent();
        intent.setAction(stirng);
        intent.putExtra("enableDecode", b);
        sendBroadcast(intent);
    }


    //发送广播启动扫描状态
    private void sendBroadcast2(String stirng) {
        Intent intent = new Intent();
        intent.setAction(stirng);
        sendBroadcast(intent);
    }




    //停止后2s恢复连续扫描勾选
    private void initScanTime() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 *要执行的操作
                 */
                //停止扫描前设置这个
                sendBroadcast("com.setscan.continuous", true);
            }
        }, 2000); //2秒后执行Runnable中的run方法,否则初始化失败
    }


    //清空目录下文件
    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
    //    dir.delete();// 删除目录本身
    }


    /**
     * 删除数据
     * @return 数组
     */
    private void delData() {
        ProgressDialogUtils.showProgressDialog(this, "正在删除图片数据，请稍候");
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteDir("/sdcard/Boohee/");
                handler.sendEmptyMessage(3);
            }
        }).start();
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止
        handler.removeCallbacksAndMessages(null);
    }
}
