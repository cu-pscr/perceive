package org.appspot.apprtc.perceive;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.appspot.apprtc.CallActivity;
import org.appspot.apprtc.ConnectActivity;

public class RTCInferService extends Service implements MobileInsightReceiver.MobileInsightListener, Runnable {

    public static final String ACTION_CONNECT = "org.appspot.apprtc.perceive.START";
    public static final String ACTION_DISCONNECT = "org.appspot.apprtc.perceive.STOP";

    public static boolean isRunning = false;
    private Gson gson;
    private InferenceMethod imethod;
    private MobileInsightReceiver mReceiver;

    // public static int predictedAdjustedBitrate = -1;
    // public static int mismatchFixedBitrate = -1;

    // public static int webrtcAdjustedBitrate = -1;

    private static final int MAX_CELLINFO_LIST_SIZE = 100;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("RTCInferService", "onStartCommand");
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            isRunning = false;
            return START_NOT_STICKY;
        } else if (intent != null && ACTION_CONNECT.equals(intent.getAction())) {
            isRunning = true;
            try {
                int connect_type = intent.getIntExtra(CallActivity.EXTRA_CONNECT_TYPE, ConnectActivity.CONNECT_FBCC);
                switch(connect_type) {
                case ConnectActivity.CONNECT_PERCEIVE:
                    imethod = new InferenceTF(RTCInferService.this);
                    break;
                case ConnectActivity.CONNECT_FBCC:
                    imethod = new InferenceFBCC();
                    break;
                }
                Thread t = new Thread(this);
                t.start();
            } catch (Exception e) {
            }
            return START_NOT_STICKY;
        }
        isRunning = false;
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i("RTCInferService", "onCreate");
        super.onCreate();
        registerMobileInsightReceiver();
    }

    @Override
    public void onDestroy() {
        Log.i("RTCInferService", "onDestroy");
        super.onDestroy();
        isRunning = false;
        unregisterMobileInsightReceiver();
    }

    @Override
    public void onMessage(String msg) {
        Log.i("RTCInferService", "msg : " + msg);
        CellInfo.updateCellInfo(msg);
        InputInfoManager.updateInfo(RTCInferService.this);
    }

    @Override
    public void run() {
        while(isRunning) {
            long stime = System.currentTimeMillis();
            if (imethod != null) {
                float tbs = imethod.inferTBS();
                int temp = (int)((tbs * 8) * 1000);
                RTSetting.predictedAdjustedBitrate = temp;

                Log.i("RTCInferService", "Prediction TBS : " + tbs);
                Log.i("RTCInferService", "Prediction Bitrate : " + RTSetting.predictedAdjustedBitrate);
            }
            long ctime = System.currentTimeMillis();
            long t = 100 - (ctime - stime);
            Log.i("RTCInferService", "Prediction Time : " + (ctime - stime));

            // if (temp - predictedAdjustedBitrate > 500000) {
            //     predictedAdjustedBitrate += 500000;
            // } else {
            //     predictedAdjustedBitrate = temp;
            // }
            // // if (temp - predictedAdjustedBitrate < (-1 * 500000)) {
            // //     predictedAdjustedBitrate -= 500000;
            // // }

            if (t > 0) {
                try {
                    Thread.sleep(t);
                } catch (InterruptedException ignore) { }

            }
        }
    }

    public void registerMobileInsightReceiver() {
        gson = new Gson();
        Log.i("MobileInsightReceiver", "== register");
        IntentFilter filter = new IntentFilter();
        filter.addAction("MobileInsight.LoggingAnalyzer.INFO");
        mReceiver = new MobileInsightReceiver();
        mReceiver.setMobileInsightListener(this);
        this.registerReceiver(mReceiver, filter);
    }

    public void unregisterMobileInsightReceiver() {
        Log.i("MobileInsightReceiver", "== unregister");
        if (mReceiver != null)
            this.unregisterReceiver(mReceiver);
        mReceiver = null;
    }
}
