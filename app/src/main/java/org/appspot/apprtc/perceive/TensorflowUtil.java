package org.appspot.apprtc.perceive;

import android.content.Context;
import android.util.Log;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TensorflowUtil {

    String modelFilename1 = "lstm_model_100ms_100ms_20.pb";
    String modelFilename2 = "lstm_model_300ms_100ms_20.pb";
    // String modelFilename3 = "lstm_model_1000ms_100ms_20.pb";
    // String modelFilename3 = "lstm2_model_1000ms_small2_data2_lstm_20_1000ms_100ms.pb";
    String modelFilename3 = "lstm2_model_1000ms_small3_data2_lstm_20_1000ms_100ms.pb";

    // String modelFilename = "lstm_model_1000ms.pb";
    String inputName = "lstm_1_input";
    String outputName = "output_node0";

    String[] outputNames = new String[] {outputName};

    TensorFlowInferenceInterface tfHelper1 = null;
    TensorFlowInferenceInterface tfHelper2 = null;
    TensorFlowInferenceInterface tfHelper3 = null;

    int num_output = 1;

    public TensorflowUtil(Context context) {
        tfHelper1 = new TensorFlowInferenceInterface(context.getResources().getAssets(), modelFilename1);
        tfHelper2 = new TensorFlowInferenceInterface(context.getResources().getAssets(), modelFilename2);
        tfHelper3 = new TensorFlowInferenceInterface(context.getResources().getAssets(), modelFilename3);
        Log.i("TensorflowUtil", "=== init");
    }

    public float[] infer(float[] input, int type) {
        float[] output = new float[num_output];
        TensorFlowInferenceInterface helper = null;
        switch (type) {
        case 100:
            helper = tfHelper1;
            break;
        case 300:
            helper = tfHelper2;
            break;
        case 1000:
            helper = tfHelper3;
            break;
        }
        //put the input data for prediction
        helper.feed(inputName,input,1,type/20,3);
        //Prediction
        helper.run(outputNames);
        //Get output from outputName which contains prediction results
        helper.fetch(outputName, output);
        //get results
        // Log.i("TensorflowUtil", "=== infer : " + output);
        return output;
    }

}
