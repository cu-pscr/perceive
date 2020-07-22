# PerceiveAndroid

PERCEIVE(Predicted pERformance by CEllular Inferring deVicE)[1] is a deep learning-based uplink
throughput prediction framework which utilizes Long Short-Term Memory (LSTM)-based neural network.

This project applies the PERCEIVE framework to AppRTCDemo of WebRTC[2] to enable the low latency video streaming.
This project cooperates with MobileInsight[3].

## Install MobileInsight
1) make your phone rooted
2) adb install MobileInsight-3.4.0-debug.apk
3) Run mobileinsight with PERCEIVE plugin

## Build
1) git clone https://github.com/lovgrammer/PerceiveAndroid.git
2) import project to your Android Studio
3) build

## To utilize PERCEIVE LSTM model
copy models in 'app/src/main/assets' to your project.
Example usage of the models: 'PerceiveAndroid/app/src/main/java/org/appspot/apprtc/perceive/InferenceTF.java'


[1] https://dl.acm.org/doi/abs/10.1145/3386901.3388911

[2] https://chromium.googlesource.com/external/webrtc/stable/talk/+/master/examples/ios/AppRTCDemo

[3] http://www.mobileinsight.net/
