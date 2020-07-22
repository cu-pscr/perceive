package org.appspot.apprtc.perceive;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.appspot.apprtc.CallActivity;
import org.appspot.apprtc.ConnectActivity;

public class InferenceThread implements Runnable {

    public static long beforeTime;
    private boolean isRunning = true;
    private int connect_type = ConnectActivity.CONNECT_DEFAULT;
    private InferenceMethod imethod;

    Handler handler = new Handler();

    public InferenceThread(Context context, long beforeTime, int connect_type) {
        this.beforeTime = beforeTime;
        this.connect_type = connect_type;
        switch(connect_type) {
        case ConnectActivity.CONNECT_PERCEIVE:
            imethod = new InferenceTF(context);
            break;
        // case ConnectActivity.CONNECT_FBCC:
        //     imethod = new InferenceFBCC();
        //     break;
        }
    }

    @Override
    public void run() {
        long stime = System.currentTimeMillis();
        if (stime < beforeTime) {
            if (!isRunning) return;
            Log.i("InferenceThread", "delay1 : " + (beforeTime - stime));
            handler.postDelayed(this, beforeTime - stime);
            return;
        }
        long t = 0;
        if (RTSetting.idx != -1) {
            t = infer();
            Log.i("InferenceThread", "Itvl100 => a" + stime + ", b" + (beforeTime) + ", e : " + (stime - beforeTime));
        }

        int skip = (int)((stime + t - beforeTime) / 100);
        long temp = beforeTime;
        beforeTime = beforeTime + (100 * skip) + 100;
        if (!isRunning) return;
        Log.i("InferenceThread", "delay2 : " +(temp + (skip*100) + 100 - stime - t));
        handler.postDelayed(this, temp + (skip*100) + 100 - stime - t);
    }

    public long infer() {
        long stime = System.currentTimeMillis();
        if (imethod != null) {
            float tbs = imethod.inferTBS();
            int temp = (int)((tbs * 8) * 1000);
            RTSetting.predictedAdjustedBitrate = temp;
            Log.i("InferenceThread", "Prediction TBS : " + tbs);
            Log.i("InferenceThread", "Prediction Bitrate : " + RTSetting.predictedAdjustedBitrate);
        }
        long etime = System.currentTimeMillis();
        Log.i("InferenceThread", "inference finished - time : " + etime);
        return etime - stime;
    }

    public void finish() {
        isRunning = false;
    }

}
