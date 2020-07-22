package org.appspot.apprtc.perceive;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.Process;
import java.lang.Runnable;
import java.lang.Thread;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.appspot.apprtc.CallActivity;
import org.appspot.apprtc.ConnectActivity;
import org.appspot.apprtc.HudFragment;

public class MobileInsightService extends Service implements Runnable {

    private MobileInsightReceiver mReceiver;
    public static final String ACTION_CONNECT = "org.appspot.apprtc.perceive.START";
    public static final String ACTION_DISCONNECT = "org.appspot.apprtc.perceive.STOP";
    public static boolean isRunning = false;
    public static MappedByteBuffer mem;
    public static long beforeTime;

    InferenceThread inferThread;

    Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MobileInsightService", "onStartCommand");

        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            isRunning = false;
            return START_NOT_STICKY;
        } else if (intent != null && ACTION_CONNECT.equals(intent.getAction())) {
            StatusDataCollector.clear();
            int connect_type = intent.getIntExtra(CallActivity.EXTRA_CONNECT_TYPE, ConnectActivity.CONNECT_FBCC);
            try {
                FileChannel fc = new RandomAccessFile(new File("/sdcard/Download/perceive_pipe.txt"), "rw").getChannel();
                mem = fc.map(FileChannel.MapMode.READ_ONLY, 0, 100);
                isRunning = true;
                beforeTime = System.currentTimeMillis();
                Thread t = new Thread(this);
                t.start();

                inferThread = new InferenceThread(MobileInsightService.this, beforeTime, connect_type);
                Thread t2 = new Thread(inferThread);
                t2.start();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
            return START_NOT_STICKY;
        }
        isRunning = false;
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i("MobileInsightService", "onCreate");
        super.onCreate();
        // registerMobileInsightReceiver();
    }

    @Override
    public void onDestroy() {
        Log.i("MobileInsightService", "onDestroy");
        super.onDestroy();
        isRunning = false;
        if (inferThread != null) inferThread.finish();
        // unregisterMobileInsightReceiver();
    }

    @Override
    public void run() {
        long stime = System.currentTimeMillis();
        if (stime < beforeTime) {
            if (!isRunning) return;
            Log.i("MobileInsightService", "delay1 : " + (beforeTime - stime));
            handler.postDelayed(this, beforeTime - stime);
            return;
        }
        long t = calculate();
        int aidx = RTSetting.getIndex(Info.atime);
        int ridx = RTSetting.getIndex(Info.rtime);
        if (RTSetting.idx == -1 || RTSetting.idx != ridx) {
            RTSetting.idx = ridx;
            int eidx = RTSetting.getIndex(Info.atime - Info.rtime);
            // Log.i("MobileInsightService", "aidx : " + aidx + ", ridx : " + ridx + ", eidx : " + getIndex(Info.atime - Info.rtime));
            RTSetting.rbs[ridx] = Info.rb;
            RTSetting.tbs[ridx] = Info.tbs;
            RTSetting.rbs_diff[ridx] = Info.rb - RTSetting.rbs[RTSetting.before(ridx)];
            RTSetting.tbs_diff[ridx] = Info.tbs - RTSetting.tbs[RTSetting.before(ridx)];
            RTSetting.buffer_bytes[ridx] = Info.buffer_bytes;

            if (Info.tbs != 0) {
                RTSetting.dt = Info.buffer_bytes / Info.tbs;
            }
            RTSetting.dt_avg
                = RTSetting.dt_avg * (1.0f-RTSetting.avgp) + (RTSetting.dt) * RTSetting.avgp;
            if (RTSetting.dt_avg != 0.0f) {
                int fps_adapt = (int)(CallActivity.target_drop/(RTSetting.dt_avg/CallActivity.d_target_fps));
                if (fps_adapt > CallActivity.target_drop) fps_adapt = CallActivity.target_drop;
                if (fps_adapt < 2) fps_adapt = 2;
                RTSetting.fps_adapt = fps_adapt;
                Log.i("MobileInsightService", "dt : " + RTSetting.dt_avg + ", fps_adapt :" + fps_adapt);
            }
        }

        StatusDataCollector
            .saveBitrate(MobileInsightService.this,
                         Info.rtime,
                         Info.atime,
                         (int)(RTSetting.getTbsAvg(5)*8000),
                         RTSetting.predictedAdjustedBitrate,
                         RTSetting.mismatchFixedBitrate,
                         RTSetting.webrtcAdjustedBitrate,
                         HudFragment.current_fps,
                         RTSetting.encoding_time,
                         RTSetting.packetLoss,
                         RTSetting.rtts[RTSetting.ridx],
                         Info.buffer_bytes,
                         RTSetting.dt,
                         RTSetting.dt_avg,
                         RTSetting.gcc_fallback,
                         RTSetting.fps_adapt);

        // Log.i("MobileInsightService", "a" + Info.atime + ", b" + (beforeTime) + ", e : " + (Info.atime - beforeTime));
        int skip = (int)((stime + t - beforeTime) / 20);
        long temp = beforeTime;
        beforeTime = beforeTime + (20 * skip) + 20;
        if (!isRunning) return;
        Log.i("MobileInsightService", "delay2 : " +(temp + (skip*20) + 20 - stime - t));
        handler.postDelayed(this, temp + (skip*20) + 20 - stime - t);
    }

    public long calculate() {
        long stime = System.currentTimeMillis();
        String msg = getMMappedMessage();
        StringTokenizer st = new StringTokenizer(msg, ",");

        Info.atime = System.currentTimeMillis();

        int cnt = 0;
        try {
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                switch(cnt) {
                case 0:
                    Info.mtime = Long.parseLong(s);
                    break;
                case 1:
                    Info.rtime = Long.parseLong(s);
                    break;
                case 2:
                    Info.ctime = Long.parseLong(s);
                    break;
                case 3:
                    Info.tbs = Integer.parseInt(s);
                    break;
                case 4:
                    Info.rb = Integer.parseInt(s);
                    break;
                case 5:
                    Info.buffer_bytes = Integer.parseInt(s);
                    break;
                }
                cnt++;
            }
        } catch (Exception e) {
        }
        Log.i("MobileInsightService", "mtime : " + Info.mtime + ", Info.rtime : " + Info.rtime + ", ctime :" + Info.ctime + ", tbs : " + Info.tbs + ", rbs : " + Info.rb + ", buffer_bytes : " + Info.buffer_bytes);
        return System.currentTimeMillis() - stime;
    }

    public String getMMappedMessage() {
        if (mem != null) {
            CharBuffer charBuffer = Charset.forName("UTF-8").decode(mem);
            mem.flip();
            return charBuffer.toString();
        }
        Log.i("MobileInsightService", "=== null");
        return "";
    }

    public static class Info {
        public static long atime;
        public static long mtime;
        public static long rtime;
        public static long ctime;
        public static int tbs;
        public static int rb;
        public static int buffer_bytes;
    }


}
