package org.appspot.apprtc.perceive;

import android.content.Context;

public class InferenceRF implements InferenceMethod {

    public InferenceRF(Context context) {

    }

    @Override
    public int inferDynamics() {
        return 1;
    }

    @Override
    public float inferTBS() {
        return 0.0f;
    }
}
