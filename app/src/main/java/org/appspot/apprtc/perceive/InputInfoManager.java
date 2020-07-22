package org.appspot.apprtc.perceive;

import android.content.Context;
import android.util.Log;
import java.lang.Math;
import org.appspot.apprtc.HudFragment;
import org.appspot.apprtc.CallActivity;

public class InputInfoManager {

    public static float[] rbs = new float[50];
    public static float[] tbs = new float[50];
    public static float[] rbs_diff = new float[50];
    public static float[] tbs_diff = new float[50];
    public static long[] times = new long[50];
    public static int[] cnts = new int[50];
    public static int idx = 0;

    // private static float mu = 2685.02f;
    // private static float sig = 1282.25f;
    // private static float mu = 4012.46f;
    // private static float sig = 2168.21f;
    private static float mu = 1889.83f;
    private static float sig = 1077.19f;
    public static float dt = 0.0f;

    // public static long encoding_time = 0;
    // public static short packetLoss;
    // public static long roundTripTimeMs;
    public static int buffer_bytes;

    public static float datarate = 0.0f;

    public static void updateInfo(Context context) {
        long ctime = System.currentTimeMillis();
        if (ctime - times[idx] > 20 && cnts[idx] != 0) {
            rbs[idx] /= cnts[idx];
            tbs[idx] /= cnts[idx];
            rbs_diff[idx] = rbs[idx] - rbs[before(idx)];
            tbs_diff[idx] = tbs[idx] - tbs[before(idx)];

            float tbs_avg = 0;
            int ci = idx;
            for (int i=0; i<5; i++) {
                tbs_avg += tbs[ci];
                ci = before(ci);
            }
            tbs_avg /= 5;
            // StatusDataCollector.saveBitrate(context,(int)(tbs_avg * 8000), RTCInferService.predictedAdjustedBitrate, RTCInferService.webrtcAdjustedBitrate, HudFragment.current_fps, encoding_time, packetLoss, roundTripTimeMs, buffer_bytes);
            StatusDataCollector.saveBitrate(context,(int)(tbs_avg * 8000), RTSetting.mismatchFixedBitrate, RTSetting.webrtcAdjustedBitrate, HudFragment.current_fps, RTSetting.encoding_time, RTSetting.packetLoss, RTSetting.rtts[RTSetting.ridx], buffer_bytes);
            // datarate = datarate * 0.75f + (tbs[0]/20.0f) * 0.25f;
            datarate = datarate * 0.75f + (tbs[0]) * 0.25f;

            times[idx] = 0;
            cnts[idx] = 0;
            idx = next(idx);
            times[idx] = ctime;
            tbs[idx] = 0;
            // show();

        }
        if (times[idx] == 0) {
            times[idx] = ctime;
        }
        if (CellInfo.type.equals("UL")) {
            rbs[idx] += CellInfo.nRBs;
            tbs[idx] += CellInfo.tbs;
            cnts[idx] += 1;
        }
        if (CellInfo.type.equals("Buffer")) {
            buffer_bytes = CellInfo.buffer_bytes;
            // dt = (CellInfo.buffer_bytes) / (tbs[before(idx)]);
            dt = (CellInfo.buffer_bytes) / (datarate);

            // Log.i("InputInfoManager", "DT : " + dt);
            Log.i("InputInfoManager", "bufferBytes : " + CellInfo.buffer_bytes);
            int fps_adapt = (int)(CallActivity.target_drop/(dt/CallActivity.d_target_fps));
            if (fps_adapt < 2) fps_adapt = 2;
            RTSetting.fps_adapt = fps_adapt;
            // if (fps_adapt < 30) {
            //     CallActivity.fps_adapt = fps_adapt;
            // } else {
            //     CallActivity.fps_adapt += 2;
            // }
            Log.i("InuptInfoManager", "buffer_bytes : " + CellInfo.buffer_bytes + ", datarate : " + datarate + ", dt : " + dt + ", fps_adapt : " + RTSetting.fps_adapt);
        }
    }

    public static int next(int idx) {
        return (idx + 1) % 50;
    }

    public static int before(int idx) {
        idx = idx - 1;
        if (idx < 0) {
            idx = 49;
        }
        return idx;
    }

    public static float[][][] getInput(int ref_time) {
        // mu = 0;
        // for (int i=0; i<50; i++) {
        //     mu += tbs[i];
        // }
        // mu /= 50;
        // sig = 0;
        // for (int i=0; i<50; i++) {
        //     sig += ((tbs[i] - mu) * (tbs[i] - mu));
        // }
        // sig /= 50;
        // sig = (float)Math.sqrt(sig);
        // Log.i("InputInfoManager", "mu : " + mu + ", sig : " + sig);

        float[][][] input = new float[1][ref_time/20][3];
        int ci = next(idx);
        for (int i=0; i<(ref_time/20); i++) {
            input[0][i][0] = rbs_diff[ci];
            ci = next(ci);
        }
        ci = next(idx);
        for (int i=0; i<(ref_time/20); i++) {
            input[0][i][1] = tbs_diff[ci];
            ci = next(ci);
        }
        ci = next(idx);
        for (int i=0; i<(ref_time/20); i++) {
            input[0][i][2] = (tbs[ci] - mu) / sig;
            // input[0][i][2] = tbs[ci];
            ci = next(ci);
        }
        return input;
    }

    public static void show() {
        // rbs
        String msg = "";
        int ci = next(idx);
        for (int i=0; i< 50; i++) {
            msg = msg + "," + rbs[ci];
            ci = next(ci);
        }
        Log.i("InputInfoManager", "[LOG] rbs : " + msg);

        msg = "";
        ci = next(idx);
        for (int i=0; i< 50; i++) {
            msg = msg + "," + tbs[ci];
            ci = next(ci);
        }
        Log.i("InputInfoManager", "[LOG] tbs : " + msg);

        msg = "";
        ci = next(idx);
        for (int i=0; i< 50; i++) {
            msg = msg + "," + rbs_diff[ci];
            ci = next(ci);
        }
        Log.i("InputInfoManager", "[LOG] rbs_diff : " + msg);

        msg = "";
        ci = next(idx);
        for (int i=0; i< 50; i++) {
            msg = msg + "," + tbs_diff[ci];
            ci = next(ci);
        }
        Log.i("InputInfoManager", "[LOG] tbs_diff : " + msg);
    }
}
