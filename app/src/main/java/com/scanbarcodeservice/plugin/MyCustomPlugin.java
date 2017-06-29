package com.scanbarcodeservice.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.barcode.HSMDecodeResult;
import com.honeywell.plugins.PluginResultListener;
import com.honeywell.plugins.SwiftPlugin;
import com.scanbarcodeservice.FxService;
import com.scanbarcodeservice.R;

import java.util.List;

/* 
 * This plug-in does nothing more than render text in the middle of the screen and silently return decode results to its listeners 
 */
public class MyCustomPlugin extends SwiftPlugin {
    private TextView tvMessage;
    private String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private int clickCount = 0;
    Context context;

    public MyCustomPlugin(Context context) {
        super(context);
        this.context = context;
        //inflate the base UI layer
        View.inflate(context, R.layout.my_custom_plugin, this);

        tvMessage = (TextView) findViewById(R.id.textViewMsg);

        Button buttonHello = (Button) findViewById(R.id.buttonHello);
        buttonHello.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                tvMessage.setText("Custom Plugin: UI button clicked " + ++clickCount + " times");
            }
        });
    }

    @Override
    public void onDecode(HSMDecodeResult[] results) {
        super.onDecode(results);

        notifyListeners(results);
    }

    @Override
    protected void onDecodeFailed() {
        super.onDecodeFailed();
    }

    @Override
    protected void onImage(byte[] image, int width, int height) {
        super.onImage(image, width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void notifyListeners(HSMDecodeResult[] results) {
        //tells all plug-in monitor event listeners we have a result (used by the system)
        this.finish();

        if (results.length > 0) {
            HSMDecodeResult firstResult = results[0];
            Intent i = new Intent(context, FxService.class);
            String s = firstResult.getBarcodeData();
            if (firstResult.getBarcodeData().equals(s)) {
                // 注册系统广播 接受扫描到的数据
                Intent intent = new Intent(RECE_DATA_ACTION);
                intent.putExtra("se4500", firstResult.getBarcodeData());
                context.sendBroadcast(intent);
            }
        }


        //notify all plug-in listeners we have a result
        List<PluginResultListener> listeners = this.getResultListeners();
        for (PluginResultListener listener : listeners)
            ((MyCustomPluginResultListener) listener).onCustomPluginResult(results);
    }
}
