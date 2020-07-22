package org.appspot.apprtc.perceive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.appspot.apprtc.R;

public class InferenceTF implements InferenceMethod {

    private TensorflowLiteUtil tfUtil;

    private float[] avgs;
    private float avg_min;
    private int idx;

    public InferenceTF(Context context) {
        tfUtil = new TensorflowLiteUtil(context);
        idx = 0;
        avgs = new float[5];

    }

    @Override
    public int inferDynamics() {
        return 1;
    }

    int cnt = 0;

    @Override
    public float inferTBS() {
        int srst = tfUtil.inferSelection(RTSetting.getInput(1000));
        float result = 0;
        switch (srst) {
        case 0:
            result = tfUtil.infer(RTSetting.getInput(100), 100);
            break;
        case 1:
            result = tfUtil.infer(RTSetting.getInput(300), 300);
            break;
        case 2:
            result = tfUtil.infer(RTSetting.getInput(1000), 1000);
            break;
        }
        // float dt = InputInfoManager.dt;
        // Log.i("InferenceTF", "dt : " + dt + ", adapt : " + adapt);
        // if (dt <= 20) {
        //     adapt *= alpha;
        // } else {
        //     adapt *= beta;
        // }
        // if (adapt > upper) {
        //     adapt = upper;
        // } else if (adapt < lower) {
        //     adapt = lower;
        // }
        // return result * adapt;
        return result;
        // int ref_time = 100;
        // float bwPW = tfUtil.infer(InputInfoManager.getInput(ref_time), ref_time);

        // return bwPW * 0.7f;
        // return bwPW * adapt;
        // return (125 * 4);

        // float tbs_avg = 0;
        // float[][][] input = InputInfoManager.getInput(1000);
        // for (int i=0; i<5; i++) {
        //     tbs_avg += input[0][48-i][2];
        // }
        // tbs_avg /= 5;
        // avgs[idx] = tbs_avg;
        // int ci = idx;
        // float temp = avgs[idx];
        // for (int i=0; i<5; i++) {
        //     if (avgs[ci] != 0 && avgs[ci] < temp) {
        //         temp = avgs[ci];
        //     }
        //     ci = next(idx);
        // }
        // idx = next(idx);
        // if (temp - avg_min > 10) {
        //     avg_min += 10;
        // }
        // if (temp - avg_min < 10) {
        //     avg_min -= 10;
        // }
        // return avg_min;

        // float[][][] input = InputInfoManager.getInput(1000);
        // float min = input[0][0][2];
        // for (int i=0; i<50; i++) {
        //     if (input[0][i][2] < min) {
        //         min = input[0][i][2];
        //     }
        // }
        // mins[idx] = min;
        // float gmin = mins[idx];
        // int ci = idx;
        // for (int i=0; i<5; i++) {
        //     if (mins[ci] < gmin) {
        //         gmin = mins[ci];
        //     }
        //     ci = next(idx);
        // }
        // idx = next(idx);
        // return gmin;

        // cnt++;
        // if (12.5 * (int)(cnt/20) < 125 * 10) {
        //     return (12.5f) * (int)(cnt/20);
        // }
        // if ((cnt/20) % 2 == 0) {
        //     return 125f;
        // }
        // return (125f * 10) - (125f);
        // return (125f * 10);
    }

    public static int next(int idx) {
        return (idx + 1) % 5;
    }

    public static int before(int idx) {
        idx = idx - 1;
        if (idx < 0) {
            idx = 4;
        }
        return idx;
    }
}
