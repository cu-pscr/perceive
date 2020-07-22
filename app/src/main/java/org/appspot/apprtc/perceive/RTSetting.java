package org.appspot.apprtc.perceive;

public class RTSetting {

    public static int predictedAdjustedBitrate = -1;
    public static int mismatchFixedBitrate = -1;
    public static int webrtcAdjustedBitrate = -1;
    public static long encoding_time = 0;
    public static short packetLoss;
    // public static long roundTripTimeMs;

    public static float[] rbs = new float[50];
    public static float[] tbs = new float[50];
    public static float[] rbs_diff = new float[50];
    public static float[] tbs_diff = new float[50];
    public static int[] buffer_bytes = new int[50];
    public static float datarate = 0.0f;
    public static float dt = 0.0f;
    public static float dt_avg = 0.0f;
    public static float avgp = 0.25f;
    public static int fps_adapt = 10;
    public static int gcc_fallback = 1;

    public static int idx = -1;
    private static float mu = 1889.83f;
    private static float sig = 1077.19f;

    public static long[] rtts = new long[10];
    public static int ridx = 0;
    public static long min_rtt = 0;

    public static float[][][] getInput(int ref_time) {
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

    public static int next_ridx(int idx) {
        return (idx + 1) % 10;
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

    public static int getIndex(long time) {
        return (int)((time % 1000) / 20);
    }

    public static float getTbsAvg(int count) {
        float tbs_avg = 0;
        int ci = idx;
        for (int i=0; i<count; i++) {
            tbs_avg += tbs[ci];
            ci = before(ci);
        }
        tbs_avg /= count;
        return tbs_avg;
    }
}
