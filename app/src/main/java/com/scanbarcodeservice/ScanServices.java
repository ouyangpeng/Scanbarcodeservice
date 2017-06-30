package com.scanbarcodeservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.honeywell.barcode.ActiveCamera;
import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.barcode.HSMDecoder;
import com.honeywell.camera.CameraManager;
import com.honeywell.license.ActivationManager;
import com.honeywell.license.ActivationResult;
import com.honeywell.plugins.PluginManager;
import com.honeywell.plugins.decode.DecodeResultListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.attr.path;
import static com.honeywell.barcode.Symbology.AZTEC;
import static com.honeywell.barcode.Symbology.C128_ISBT;
import static com.honeywell.barcode.Symbology.CODABAR;
import static com.honeywell.barcode.Symbology.CODABLOCK_F;
import static com.honeywell.barcode.Symbology.CODE11;
import static com.honeywell.barcode.Symbology.CODE128;
import static com.honeywell.barcode.Symbology.CODE39;
import static com.honeywell.barcode.Symbology.CODE93;
import static com.honeywell.barcode.Symbology.COMPOSITE;
import static com.honeywell.barcode.Symbology.COMPOSITE_WITH_UPC;
import static com.honeywell.barcode.Symbology.COUPON_CODE;
import static com.honeywell.barcode.Symbology.DATAMATRIX;
import static com.honeywell.barcode.Symbology.DATAMATRIX_RECTANGLE;
import static com.honeywell.barcode.Symbology.EAN13;
import static com.honeywell.barcode.Symbology.EAN13_2CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.EAN13_5CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.EAN13_ISBN;
import static com.honeywell.barcode.Symbology.EAN8;
import static com.honeywell.barcode.Symbology.EAN8_2CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.EAN8_5CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.GS1_128;
import static com.honeywell.barcode.Symbology.HANXIN;
import static com.honeywell.barcode.Symbology.HK25;
import static com.honeywell.barcode.Symbology.I25;
import static com.honeywell.barcode.Symbology.IATA25;
import static com.honeywell.barcode.Symbology.KOREA_POST;
import static com.honeywell.barcode.Symbology.M25;
import static com.honeywell.barcode.Symbology.MAXICODE;
import static com.honeywell.barcode.Symbology.MICROPDF;
import static com.honeywell.barcode.Symbology.MSI;
import static com.honeywell.barcode.Symbology.OCR;
import static com.honeywell.barcode.Symbology.PDF417;
import static com.honeywell.barcode.Symbology.QR;
import static com.honeywell.barcode.Symbology.RSS_14;
import static com.honeywell.barcode.Symbology.RSS_EXPANDED;
import static com.honeywell.barcode.Symbology.RSS_LIMITED;
import static com.honeywell.barcode.Symbology.S25;
import static com.honeywell.barcode.Symbology.TELEPEN;
import static com.honeywell.barcode.Symbology.TRIOPTIC;
import static com.honeywell.barcode.Symbology.UPCA;
import static com.honeywell.barcode.Symbology.UPCA_2CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.UPCA_5CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.UPCE0;
import static com.honeywell.barcode.Symbology.UPCE1;
import static com.honeywell.barcode.Symbology.UPCE_2CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.UPCE_5CHAR_ADDENDA;
import static com.honeywell.barcode.Symbology.UPCE_EXPAND;


/**
 * Created by suntianwei on 2017/3/16.
 */

public class ScanServices extends Service implements DecodeResultListener {
    private HSMDecoder hsmDecoder;
    private String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private String START_SCAN_ACTION = "com.geomobile.se4500barcode";
    private String STOP_SCAN_ACTION = "com.geomobile.se4500barcodestop";
    private String OPEN_CAMERA = "com.se4500.opencamera";
    private String CLOSE_CAMERA = "com.se4500.closecamera";
    private Handler handler;
    private CameraManager cameraManager;
    private Vibrator vibrator;
    private SharedPreferencesUitl preferencesUitl;
    private String TAG = "scan";
    private String[] array;
    private boolean[] isDecodeFlag;

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesUitl = SharedPreferencesUitl.getInstance(this, "setscan");
        initAPI();
        hsmDecoder.setActiveCamera(ActiveCamera.FRONT_FACING);
        handler = new Handler();
        intentFilter();
        //震动
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
    }


    private void initAPI() {
        try {
            //activate the API with your license key
            ActivationResult activationResult = ActivationManager.activate(this, "trial-speed-tjian-03162017");
            Toast.makeText(this, "Activation Result: " + activationResult, Toast.LENGTH_LONG).show();
            //get the singleton instance of the decoder
            hsmDecoder = HSMDecoder.getInstance(this);
//            hsmDecoder.setActiveCamera(ActiveCamera.FRONT_FACING);
            //set all decoder related settings
//            hsmDecoder.enableSymbology(Symbology.UPCA);
//            hsmDecoder.enableSymbology(Symbology.CODE128);
//            hsmDecoder.enableSymbology(Symbology.CODE39);
//            hsmDecoder.enableSymbology(Symbology.QR);
//            hsmDecoder.enableSymbology(EAN13);
//            hsmDecoder.enableSymbology(Symbology.CODE39);
//            hsmDecoder.enableSymbology(Symbology.CODABAR);
//            hsmDecoder.enableSymbology(Symbology.EAN13_2CHAR_ADDENDA);
//            hsmDecoder.enableSymbology(Symbology.UPCA_2CHAR_ADDENDA);
//            hsmDecoder.enableSymbology(Symbology.SYMS);
//            hsmDecoder.enableFlashOnDecode(false);
            initEnableDecode();
            hsmDecoder.enableSound(true);
//            enableDecodeFlag();
            hsmDecoder.enableAimer(true);
            hsmDecoder.setAimerColor(Color.RED);
            hsmDecoder.setOverlayText(getString(R.string.show_information));
            hsmDecoder.setOverlayTextColor(Color.WHITE);
            cameraManager = CameraManager.getInstance(this);
            hsmDecoder.addResultListener(this);
            //create plug-in instance and add a result listener
//            customPlugin = new MyCustomPlugin(this);
//            customPlugin.addResultListener(this);

            //register the plug-in with the system
//            hsmDecoder.registerPlugin(customPlugin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_SCAN_ACTION);
        filter.addAction(RECE_DATA_ACTION);
        filter.addAction(OPEN_CAMERA);
        filter.addAction(CLOSE_CAMERA);
        filter.addAction("keycode.f4.down");
        filter.addAction("com.setscan.enablescan");
        filter.addAction("com.setscan.showdecode");
        filter.addAction("com.setscan.sound");
        filter.addAction("com.setscan.vibrator");
        filter.addAction("com.setscan.flash");
        filter.addAction("com.setscan.continuous");
        filter.addAction("com.setscan.qianzhui");
        filter.addAction("com.setscan.houzhui");
        filter.addAction("com.setscan.decodetype");
        filter.addAction("com.setscan.issaveimage");
        registerReceiver(receiver, filter);
    }

    private static String isEnable = "isenable";
    private static String isShowdecode = "isshowdecode";
    private static String isSound = "issound";
    private static String isVibrator = "isvibrator";
    private static String isFlash = "isflash";
    private static String isContinuous = "iscontinuous";
    private static String qianzhui = "qianzhui";
    private static String houzhui = "houzhui";
    private static String decodeFlag = "decodeflag";
    private static String enableFlag = "enableflag";
    private static String isSaveImage = "issaveimage";

    private static Intent Myintent = new Intent();


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getAction();
            if (state.equals(START_SCAN_ACTION) || state.equals("keycode.f4.down")) {
                Myintent.setClass(context, FxService.class);
                context.startService(Myintent);
//                handler.removeCallbacks(runnable);
//                handler.postDelayed(runnable, 5000);
            } else if (state.equals(OPEN_CAMERA)) {
                cameraManager.closeCamera();
                SystemProperties.set("persist.sys.iscamera", "open");
                context.stopService(Myintent);
            } else if (state.equals(CLOSE_CAMERA)) {
//                cameraManager.reopenCamera();
                SystemProperties.set("persist.sys.iscamera", "close");
            } else if (intent.getAction().equals(STOP_SCAN_ACTION)) {
                context.stopService(Myintent);
            }
            switch (state) {
                case "com.setscan.enablescan":
//                    isVibrator = (Boolean) intent.getExtras().get("enableDecode");
                    break;
                case "com.setscan.showdecode":
                    preferencesUitl.write(isShowdecode, (Boolean) intent.getExtras().get("enableDecode"));
                    break;
                case "com.setscan.sound":
                    preferencesUitl.write(isSound, (Boolean) intent.getExtras().get("enableDecode"));
                    hsmDecoder.enableSound(preferencesUitl.read(isSound, true));
                    break;
                case "com.setscan.vibrator":
                    preferencesUitl.write(isVibrator, (Boolean) intent.getExtras().get("enableDecode"));
                    break;
                case "com.setscan.flash":
                    preferencesUitl.write(isFlash, intent.getBooleanExtra("enableDecode", false));
                    hsmDecoder.enableFlashOnDecode(preferencesUitl.read(isFlash, true));
                    break;
                case "com.setscan.continuous":
                    preferencesUitl.write(isContinuous, intent.getBooleanExtra("enableDecode", false));
//                    b = (Boolean) intent.getExtras().get("enableDecode");
                    break;
                case "com.setscan.qianzhui":
                    String ss = intent.getExtras().getString("enableDecode");
                    preferencesUitl.write(qianzhui, ss);
                    break;
                case "com.setscan.houzhui":
                    preferencesUitl.write(houzhui, intent.getExtras().getString("enableDecode"));
                    break;
                case "com.setscan.issaveimage":
                    preferencesUitl.write(isSaveImage, intent.getBooleanExtra("enableDecode", true));
                    break;
                case "com.setscan.decodetype":
                    Bundle bundle = intent.getExtras();
                    array = bundle.getStringArray("enableDecode");
                    isDecodeFlag = bundle.getBooleanArray("enableflag");
                    Log.i(TAG, "s.lengh" + array.length + "");
                    for (int i = 0; i < array.length; i++) {
                        Log.i(TAG, "items: " + array[i]);
                        preferencesUitl.write(decodeFlag + i, array[i]);
                    }
                    for (int i = 0; i < isDecodeFlag.length; i++) {
                        preferencesUitl.write(enableFlag + i, isDecodeFlag[i]);
                    }
                    enableDecodeFlag();
                    break;

            }
        }
    };

    /**
     * 使能/非使能条码类型
     */
    private void enableDecodeFlag() {
        for (int i = 0; i < array.length; i++) {
            boolean b = preferencesUitl.read(enableFlag + i, true);
            switch (preferencesUitl.read(decodeFlag + i, "")) {
                case "UPCA":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCA);
                    } else {
                        hsmDecoder.disableSymbology(UPCA);
                    }
                    break;
                case "UPCA_2CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCA_2CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(UPCA_2CHAR_ADDENDA);
                    }
                    break;
                case "UPCA_5CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCA_5CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(UPCA_5CHAR_ADDENDA);
                    }
                    break;
                case "UPCE0":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCE0);
                    } else {
                        hsmDecoder.disableSymbology(UPCE0);
                    }
                    break;
                case "UPCE1":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCE1);
                    } else {
                        hsmDecoder.disableSymbology(UPCE1);
                    }
                    break;
                case "UPCE_EXPAND":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCE_EXPAND);
                    } else {
                        hsmDecoder.disableSymbology(UPCE_EXPAND);
                    }
                    break;
                case "UPCE_2CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCE_2CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(UPCE_2CHAR_ADDENDA);
                    }
                    break;
                case "UPCE_5CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(UPCE_5CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(UPCE_5CHAR_ADDENDA);
                    }
                    break;
                case "EAN8":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN8);
                    } else {
                        hsmDecoder.disableSymbology(EAN8);
                    }
                    break;
                case "EAN8_2CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN8_2CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(EAN8_2CHAR_ADDENDA);
                    }
                    break;
                case "EAN8_5CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN8_5CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(EAN8_5CHAR_ADDENDA);
                    }
                    break;
                case "EAN13":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN13);
                    } else {
                        hsmDecoder.disableSymbology(EAN13);
                    }
                    break;
                case "EAN13_2CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN13_2CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(EAN13_2CHAR_ADDENDA);
                    }
                    break;
                case "EAN13_5CHAR_ADDENDA":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN13_5CHAR_ADDENDA);
                    } else {
                        hsmDecoder.disableSymbology(EAN13_5CHAR_ADDENDA);
                    }
                    break;
                case "EAN13_ISBN":
                    if (b) {
                        hsmDecoder.enableSymbology(EAN13_ISBN);
                    } else {
                        hsmDecoder.disableSymbology(EAN13_ISBN);
                    }
                    break;
                case "CODE128":
                    if (b) {
                        hsmDecoder.enableSymbology(CODE128);
                    } else {
                        hsmDecoder.disableSymbology(CODE128);
                    }
                    break;
                case "GS1_128":
                    if (b) {
                        hsmDecoder.enableSymbology(GS1_128);
                    } else {
                        hsmDecoder.disableSymbology(GS1_128);
                    }
                    break;
                case "C128_ISBT":
                    if (b) {
                        hsmDecoder.enableSymbology(C128_ISBT);
                    } else {
                        hsmDecoder.disableSymbology(C128_ISBT);
                    }
                    break;
                case "CODE39":
                    if (b) {
                        hsmDecoder.enableSymbology(CODE39);
                    } else {
                        hsmDecoder.disableSymbology(CODE39);
                    }
                    break;
                case "COUPON_CODE":
                    if (b) {
                        hsmDecoder.enableSymbology(COUPON_CODE);
                    } else {
                        hsmDecoder.disableSymbology(COUPON_CODE);
                    }
                    break;
                case "TRIOPTIC":
                    if (b) {
                        hsmDecoder.enableSymbology(TRIOPTIC);
                    } else {
                        hsmDecoder.disableSymbology(TRIOPTIC);
                    }
                    break;
                case "I25":
                    if (b) {
                        hsmDecoder.enableSymbology(I25);
                    } else {
                        hsmDecoder.disableSymbology(I25);
                    }
                    break;
                case "S25":
                    if (b) {
                        hsmDecoder.enableSymbology(S25);
                    } else {
                        hsmDecoder.disableSymbology(S25);
                    }
                    break;
                case "IATA25":
                    if (b) {
                        hsmDecoder.enableSymbology(IATA25);
                    } else {
                        hsmDecoder.disableSymbology(IATA25);
                    }
                    break;
                case "M25":
                    if (b) {
                        hsmDecoder.enableSymbology(M25);
                    } else {
                        hsmDecoder.disableSymbology(M25);
                    }
                    break;
                case "CODE93":
                    if (b) {
                        hsmDecoder.enableSymbology(CODE93);
                    } else {
                        hsmDecoder.disableSymbology(CODE93);
                    }
                    break;
                case "CODE11":
                    if (b) {
                        hsmDecoder.enableSymbology(CODE11);
                    } else {
                        hsmDecoder.disableSymbology(CODE11);
                    }
                    break;
                case "CODABAR":
                    if (b) {
                        hsmDecoder.enableSymbology(CODABAR);
                    } else {
                        hsmDecoder.disableSymbology(CODABAR);
                    }
                    break;
//                case "EAN13":
//                    if (b) {
//                        hsmDecoder.enableSymbology(EAN13);
////                        if ( hsmDecoder.isSymbologyEnabled(EAN13)) {
////                            Toast.makeText(ScanServices.this, "成功", Toast.LENGTH_SHORT).show();
////                        }else {
////                            Toast.makeText(ScanServices.this, "失败", Toast.LENGTH_SHORT).show();
////
////                        }
//
//                        Toast.makeText(ScanServices.this, "EAN13", Toast.LENGTH_SHORT).show();
//                    }else {
//                        hsmDecoder.disableSymbology(EAN13);
//                        Toast.makeText(ScanServices.this, "disableEAN13", Toast.LENGTH_SHORT).show();
//                    }
//                    break;
                case "TELEPEN":
                    if (b) {
                        hsmDecoder.enableSymbology(TELEPEN);
                    } else {
                        hsmDecoder.disableSymbology(TELEPEN);
                    }
                    break;
                case "MSI":
                    if (b) {
                        hsmDecoder.enableSymbology(MSI);
                    } else {
                        hsmDecoder.disableSymbology(MSI);
                    }
                    break;
                case "RSS_14":
                    if (b) {
                        hsmDecoder.enableSymbology(RSS_14);
                    } else {
                        hsmDecoder.disableSymbology(RSS_14);
                    }
                    break;
                case "RSS_LIMITED":
                    if (b) {
                        hsmDecoder.enableSymbology(RSS_LIMITED);
                    } else {
                        hsmDecoder.disableSymbology(RSS_LIMITED);
                    }
                    break;
                case "RSS_EXPANDED":
                    if (b) {
                        hsmDecoder.enableSymbology(RSS_EXPANDED);
                    } else {
                        hsmDecoder.disableSymbology(RSS_EXPANDED);
                    }
                    break;
                case "CODABLOCK_F":
                    if (b) {
                        hsmDecoder.enableSymbology(CODABLOCK_F);
                    } else {
                        hsmDecoder.disableSymbology(CODABLOCK_F);
                    }
                    break;
                case "PDF417":
                    if (b) {
                        hsmDecoder.enableSymbology(PDF417);
                    } else {
                        hsmDecoder.disableSymbology(PDF417);
                    }
                    break;
                case "MICROPDF":
                    if (b) {
                        hsmDecoder.enableSymbology(MICROPDF);
                    } else {
                        hsmDecoder.disableSymbology(MICROPDF);
                    }
                    break;
                case "COMPOSITE":
                    if (b) {
                        hsmDecoder.enableSymbology(COMPOSITE);
                    } else {
                        hsmDecoder.disableSymbology(COMPOSITE);
                    }
                    break;
                case "COMPOSITE_WITH_UPC":
                    if (b) {
                        hsmDecoder.enableSymbology(COMPOSITE_WITH_UPC);
                    } else {
                        hsmDecoder.disableSymbology(COMPOSITE_WITH_UPC);
                    }
                    break;
                case "AZTEC":
                    if (b) {
                        hsmDecoder.enableSymbology(AZTEC);
                    } else {
                        hsmDecoder.disableSymbology(AZTEC);
                    }
                    break;
                case "MAXICODE":
                    if (b) {
                        hsmDecoder.enableSymbology(MAXICODE);
                    } else {
                        hsmDecoder.disableSymbology(MAXICODE);
                    }
                    break;
                case "DATAMATRIX":
                    if (b) {
                        hsmDecoder.enableSymbology(DATAMATRIX);
                    } else {
                        hsmDecoder.disableSymbology(DATAMATRIX);
                    }
                    break;
                case "DATAMATRIX_RECTANGLE":
                    if (b) {
                        hsmDecoder.enableSymbology(DATAMATRIX_RECTANGLE);
                    } else {
                        hsmDecoder.disableSymbology(DATAMATRIX_RECTANGLE);
                    }
                    break;
                case "QR":
                    if (b) {
                        hsmDecoder.enableSymbology(QR);
                    } else {
                        hsmDecoder.disableSymbology(QR);
                    }
                    break;
                case "HANXIN":
                    if (b) {
                        hsmDecoder.enableSymbology(HANXIN);
                    } else {
                        hsmDecoder.disableSymbology(HANXIN);
                    }
                    break;
                case "HK25":
                    if (b) {
                        hsmDecoder.enableSymbology(HK25);
                    } else {
                        hsmDecoder.disableSymbology(HK25);
                    }
                    break;
                case "KOREA_POST":
                    if (b) {
                        hsmDecoder.enableSymbology(KOREA_POST);
                    } else {
                        hsmDecoder.disableSymbology(KOREA_POST);
                    }
                    break;
                case "OCR":
                    if (b) {
                        hsmDecoder.enableSymbology(OCR);
                    } else {
                        hsmDecoder.disableSymbology(OCR);
                    }
                    break;
            }
        }


    }

    private void initEnableDecode() {
        hsmDecoder.enableSymbology(UPCA);
        hsmDecoder.enableSymbology(UPCA_2CHAR_ADDENDA);
        hsmDecoder.enableSymbology(UPCA_5CHAR_ADDENDA);
        hsmDecoder.enableSymbology(UPCE_EXPAND);
        hsmDecoder.enableSymbology(UPCE_2CHAR_ADDENDA);
        hsmDecoder.enableSymbology(UPCE_5CHAR_ADDENDA);
        hsmDecoder.enableSymbology(EAN8);
        hsmDecoder.enableSymbology(EAN8_2CHAR_ADDENDA);
        hsmDecoder.enableSymbology(EAN8_5CHAR_ADDENDA);
        hsmDecoder.enableSymbology(EAN13);
        hsmDecoder.enableSymbology(EAN13_2CHAR_ADDENDA);
        hsmDecoder.enableSymbology(EAN13_5CHAR_ADDENDA);
        hsmDecoder.enableSymbology(EAN13_ISBN);
        hsmDecoder.enableSymbology(CODE128);
        hsmDecoder.enableSymbology(GS1_128);
        hsmDecoder.enableSymbology(C128_ISBT);
        hsmDecoder.enableSymbology(CODE39);
        hsmDecoder.enableSymbology(COUPON_CODE);
        hsmDecoder.enableSymbology(TRIOPTIC);
        hsmDecoder.enableSymbology(I25);
        hsmDecoder.enableSymbology(S25);
        hsmDecoder.enableSymbology(IATA25);
        hsmDecoder.enableSymbology(M25);
        hsmDecoder.enableSymbology(CODE93);
        hsmDecoder.enableSymbology(CODE11);
        hsmDecoder.enableSymbology(CODABAR);
        hsmDecoder.enableSymbology(TELEPEN);
        hsmDecoder.enableSymbology(MSI);
        hsmDecoder.enableSymbology(RSS_14);
        hsmDecoder.enableSymbology(RSS_LIMITED);
        hsmDecoder.enableSymbology(RSS_EXPANDED);
        hsmDecoder.enableSymbology(CODABLOCK_F);
        hsmDecoder.enableSymbology(PDF417);
        hsmDecoder.enableSymbology(MICROPDF);
        hsmDecoder.enableSymbology(COMPOSITE);
        hsmDecoder.enableSymbology(COMPOSITE_WITH_UPC);
        hsmDecoder.enableSymbology(AZTEC);
        hsmDecoder.enableSymbology(DATAMATRIX);
        hsmDecoder.enableSymbology(DATAMATRIX_RECTANGLE);
        hsmDecoder.enableSymbology(QR);
        hsmDecoder.enableSymbology(HANXIN);
        hsmDecoder.enableSymbology(HK25);
        hsmDecoder.enableSymbology(KOREA_POST);
        hsmDecoder.enableSymbology(OCR);
    }

    private String[] items = {"UPCA", "UPCA_2CHAR_ADDENDA", "UPCA_5CHAR_ADDENDA", "UPCE0", "UPCE1",
            "UPCE_EXPAND", "UPCE_2CHAR_ADDENDA", "UPCE_5CHAR_ADDENDA", "EAN8", "EAN8_2CHAR_ADDENDA",
            "EAN8_5CHAR_ADDENDA", "EAN13", "EAN13_2CHAR_ADDENDA", "EAN13_5CHAR_ADDENDA", "EAN13_ISBN",
            "CODE128", "GS1_128", "C128_ISBT", "CODE39", "COUPON_CODE", "TRIOPTIC", "I25", "S25", "IATA25",
            "M25", "CODE93", "CODE11", "CODABAR", "TELEPEN", "MSI", "RSS_14", "RSS_LIMITED", "RSS_EXPANDED",
            "CODABLOCK_F", "PDF417", "MICROPDF", "COMPOSITE", "COMPOSITE_WITH_UPC", "AZTEC", "MAXICODE",
            "DATAMATRIX", "DATAMATRIX_RECTANGLE", "QR", "HANXIN", "HK25", "KOREA_POST", "OCR"};
    /**
     * 停止扫描 、释放camera
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            PluginManager.stopPlugins();
            stopService(Myintent);
            cameraManager.closeCamera();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
//        hsmDecoder.disposeInstance();
        hsmDecoder.removeResultListener(this);
        stopService(Myintent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 返回解码结果
     *
     * @param hsmDecodeResults
     */
    @Override
    public void onHSMDecodeResult(HSMDecodeResult[] hsmDecodeResults) {
        displayBarcodeData(hsmDecodeResults);
    }

    private void displayBarcodeData(HSMDecodeResult[] barcodeData) {
        if (barcodeData.length > 0) {
            HSMDecodeResult firstResult = barcodeData[0];
            String qian = preferencesUitl.read(qianzhui, "");
            String hou = preferencesUitl.read(houzhui, "");
            String decodeDate = qian + firstResult.getBarcodeData() + hou;
            if (preferencesUitl.read(isShowdecode, true)) {
                senBroadcasts(decodeDate);
            }
            if (!preferencesUitl.read(isContinuous, false)) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);
            }
            if (preferencesUitl.read(isVibrator, true)) {
                vibrator.vibrate(new long[]{100, 10, 10, 100}, -1);
            }
            boolean b = preferencesUitl.read(isSaveImage, true);
            if (b) {
                saveImage(hsmDecoder.getLastBarcodeImage(firstResult.getBarcodeBounds()));
            }

        }
    }

    /**
     * 将扫描结果放在焦点上
     *
     * @param string 扫描内容
     */
    public void senBroadcasts(String string) {
        Intent intents = new Intent();
        intents.setAction(RECE_DATA_ACTION);
        intents.putExtra("se4500", string);
        sendBroadcast(intents);
    }

    /**
     * 保存扫描后的条码图片
     *
     * @param bmp
     */
    public void saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(ScanServices.this.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        ScanServices.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

}