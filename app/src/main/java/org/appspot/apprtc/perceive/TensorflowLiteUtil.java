package org.appspot.apprtc.perceive;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;

public class TensorflowLiteUtil {

    String modelFilename0 = "model_selection.tflite";
    String modelFilename1 = "model_100ms.tflite";
    String modelFilename2 = "model_300ms.tflite";
    String modelFilename3 = "model_1000ms.tflite";

    protected Interpreter tflite0;
    protected Interpreter tflite1;
    protected Interpreter tflite2;
    protected Interpreter tflite3;

    public TensorflowLiteUtil(Context context) {
        tflite0 = new Interpreter(loadModelFile(context, modelFilename0));
        tflite1 = new Interpreter(loadModelFile(context, modelFilename1));
        tflite2 = new Interpreter(loadModelFile(context, modelFilename2));
        tflite3 = new Interpreter(loadModelFile(context, modelFilename3));
    }

    private MappedByteBuffer loadModelFile(Context context, String path) {
        try {
            AssetManager assetMgr = context.getApplicationContext().getAssets();
            AssetFileDescriptor fileDescriptor = assetMgr.openFd(path);
            FileInputStream inputStream
                = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                                   startOffset, declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int inferSelection(float[][][] input) {
        float[][] output = new float[1][3];
        tflite0.run(input, output);
        int max_pos = 0;
        for (int i=0; i<3; i++) {
            if (output[0][i] > output[0][max_pos]) {
                max_pos = i;
            }
        }
        return max_pos;
    }

    public float infer(float[][][] input, int ref_time) {
        float[][] output = new float[1][1];
        switch (ref_time) {
        case 100:
            tflite1.run(input, output);
            break;
        case 300:
            tflite2.run(input, output);
            break;
        case 1000:
            tflite3.run(input, output);
            break;
        }
        return output[0][0];
    }
}
