package org.appspot.apprtc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.appspot.apprtc.perceive.TensorflowUtil;

public class TestActivity extends Activity {

    private TensorflowUtil tfUtil;
    private Button mInferButton100;
    private Button mInferButton300;
    private Button mInferButton1000;
    private TextView mResultView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mInferButton100 = (Button) findViewById(R.id.btn_infer_100);
        mInferButton300 = (Button) findViewById(R.id.btn_infer_300);
        mInferButton1000 = (Button) findViewById(R.id.btn_infer_1000);
        mInferButton100.setOnClickListener(onButtonsClick);
        mInferButton300.setOnClickListener(onButtonsClick);
        mInferButton1000.setOnClickListener(onButtonsClick);
        mResultView = (TextView) findViewById(R.id.txt_result);
        tfUtil = new TensorflowUtil(this);
    }

    OnClickListener onButtonsClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                long stime = System.currentTimeMillis();
                long result = 0;
                switch(v.getId()) {
                case R.id.btn_infer_100:
                    tfUtil.infer(genInput(100), 100);
                    result = System.currentTimeMillis() - stime;
                    mResultView.setText("" + result);
                    break;
                case R.id.btn_infer_300:
                    tfUtil.infer(genInput(300), 300);
                    result = System.currentTimeMillis() - stime;
                    mResultView.setText("" + result);
                    break;
                case R.id.btn_infer_1000:
                    tfUtil.infer(genInput(1000), 1000);
                    result = System.currentTimeMillis() - stime;
                    mResultView.setText("" + result);
                    break;
                }
            }
        };

    public float[] genInput(int type) {
        float[] input = new float[1*(type/20)*3];
        for(int i=0;i<(type/20);i++){
            for(int j=0;j<3;j++){
                input[i*j]=(float)i*j;
            }
        }
        return input;
    }
}
