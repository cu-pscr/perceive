package org.appspot.apprtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.appspot.apprtc.perceive.MobileInsightService;

public class TestBRActivity extends Activity {

    Button startButton;
    Button stopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_br);
        startButton = (Button) findViewById(R.id.btn_start);
        stopButton = (Button) findViewById(R.id.btn_stop);
        startButton.setOnClickListener(onButtonsClick);
        stopButton.setOnClickListener(onButtonsClick);
        // startMobileInsightService();
    }
    @Override
    protected void onDestroy() {
        // stopMobileInsightService();
        super.onDestroy();
    }
    OnClickListener onButtonsClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                case R.id.btn_start:
                    startMobileInsightService();
                    break;
                case R.id.btn_stop:
                    stopMobileInsightService();
                    break;
                }
            }
        };

    private void startMobileInsightService() {
        // if (connect_type == ConnectActivity.CONNECT_DEFAULT) return;
        Intent intent = new Intent(TestBRActivity.this, MobileInsightService.class);
        intent.putExtra(CallActivity.EXTRA_CONNECT_TYPE, ConnectActivity.CONNECT_PERCEIVE);
        intent.setAction(MobileInsightService.ACTION_CONNECT);
        startService(intent);
    }

    private void stopMobileInsightService() {
        // if (connect_type == ConnectActivity.CONNECT_DEFAULT) return;
        Intent intent = new Intent(TestBRActivity.this, MobileInsightService.class);
        intent.setAction(MobileInsightService.ACTION_DISCONNECT);
        stopService(intent);
    }
}
