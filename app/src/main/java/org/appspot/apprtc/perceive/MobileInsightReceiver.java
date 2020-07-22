package org.appspot.apprtc.perceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MobileInsightReceiver extends BroadcastReceiver {

    private MobileInsightListener listener;

    public interface MobileInsightListener {
        public void onMessage(String msg);
    }

    public void setMobileInsightListener(MobileInsightListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");
        // String stime = intent.getStringExtra("time");
        // long mi_time = Long.parseLong(stime);
        // long cr_time = System.currentTimeMillis();
        // Log.i("MobileInsightReceiver", "mi_time : " + mi_time);
        // Log.i("MobileInsightReceiver", "cr_time : " + cr_time);
        // StatusDataCollector.saveReceiverDelay(context, mi_time, cr_time);
        // long curr_time = System.currentTimeMillis();
        // msg = msg + "," + curr_time;
        // StatusDataCollector.saveCellInfo(context, msg);
        if (listener != null) listener.onMessage(msg);
    }


}
