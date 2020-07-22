/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.RuntimeException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.appspot.apprtc.AppRTCAudioManager.AudioDevice;
import org.appspot.apprtc.AppRTCAudioManager.AudioManagerEvents;
import org.appspot.apprtc.AppRTCClient.RoomConnectionParameters;
import org.appspot.apprtc.AppRTCClient.SignalingParameters;
import org.appspot.apprtc.PeerConnectionClient.DataChannelParameters;
import org.appspot.apprtc.PeerConnectionClient.PeerConnectionParameters;
import org.appspot.apprtc.perceive.HexUtils;
import org.appspot.apprtc.perceive.InputInfoManager;
import org.appspot.apprtc.perceive.MobileInsightService;
import org.appspot.apprtc.perceive.RTCInferService;
import org.appspot.apprtc.perceive.RTSetting;
import org.appspot.apprtc.perceive.StatusDataCollector;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraCapturer;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.JavaI420Buffer;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends Activity implements AppRTCClient.SignalingEvents,
                                                      PeerConnectionClient.PeerConnectionEvents,
                                                      CallFragment.OnCallEvents {
    private static final String TAG = "CallRTCClient";

    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
        "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
        "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
        "org.appspot.apprtc.SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
        "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
        "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
        "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
        "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
        "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG = "org.appspot.apprtc.ENABLE_RTCEVENTLOG";
    public static final String EXTRA_USE_LEGACY_AUDIO_DEVICE =
        "org.appspot.apprtc.USE_LEGACY_AUDIO_DEVICE";
    public static final String EXTRA_REMOTE = "org.appspot.apprtc.EXTRA_REMOTE";
    public static final String EXTRA_CONNECT_TYPE = "org.appspot.apprtc.EXTRA_CONNECT_TYPE";

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    private EglBase eglBase;
    private static boolean remote;
    public static int connect_type;
    private EncoderFrameCallback frameCallback;
    // public static int targetWidth = 2560;
    // public static int targetHeight = 1440;
    public static int targetWidth = 1920;
    public static int targetHeight = 1080;
    public static boolean retransmitOccured = false;
    public static int d_target = 20;
    public static int d_target_fps = 20;
    public static int target_drop = 30;
    public static int max_bitrate = 20000;

    private static class EncoderFrameCallback
        implements VideoEncoderFactory.FrameCallback {

        private int frame_count = 0;
        private VideoFileRenderer saver;
        private Context context;
        public static MappedByteBuffer mem;

        private PeerConnectionClient conClient;
        private float alpha = 1.05f;
        private float beta = 0.8f;
        private float upper = 0.9f;
        private float lower = 0.5f;
        private int gccFallbackRTT = 150;
        private int gccFallbackDt = 10;
        private SharedPreferences sharedPref;
        public static float adapt = 1.0f;
        public static boolean fallbackFlag = false;
        public static float minRTTParam = 1.5f;

        public void setPeerConnectionClient(PeerConnectionClient conClient) {
            this.conClient = conClient;
            try {
            FileChannel fc = new RandomAccessFile(new File("/sdcard/Download/webrtc_pipe.txt"), "rw").getChannel();
            mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, 100);
            } catch(FileNotFoundException e) {
            } catch(IOException e) {
            }
            setMMappedMessage("1111,0,0,");
        }

        public EncoderFrameCallback(Context context) {
            this.context = context;
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String keyprefAlpha = context.getString(R.string.pref_alpha_key);
            String keyprefBeta = context.getString(R.string.pref_beta_key);
            String keyprefUpper = context.getString(R.string.pref_upper_key);
            String keyprefLower = context.getString(R.string.pref_lower_key);
            String keyPrefDTarget = context.getString(R.string.pref_dttarget_key);
            String keyPrefDTargetFPS = context.getString(R.string.pref_dttargetforfps_key);
            String keyPrefMaxBitrate
                = context.getString(R.string.pref_maxvideobitratevalue_key);
            String keyPrefTargetDrop
                = context.getString(R.string.pref_targetfps_key);
            String keyPrefGccFallbackRTT
                = context.getString(R.string.pref_gccfallback_rtt_key);
            String keyPrefGccFallbackDt
                = context.getString(R.string.pref_gccfallback_dt_key);
            String keyPrefDTAvgParam
                = context.getString(R.string.pref_dtavg_param_key);
            String keyPrefMinRTTParam
                = context.getString(R.string.pref_gccfallback_minrttparam_key);

            alpha = Float.parseFloat(sharedPref.getString(keyprefAlpha, "1.05"));
            beta = Float.parseFloat(sharedPref.getString(keyprefBeta, "0.8"));
            upper = Float.parseFloat(sharedPref.getString(keyprefUpper, "0.9"));
            lower = Float.parseFloat(sharedPref.getString(keyprefLower, "0.5"));
            d_target = Integer.parseInt(sharedPref.getString(keyPrefDTarget, "20"));
            d_target_fps = Integer.parseInt(sharedPref.getString(keyPrefDTargetFPS, "20"));
            max_bitrate
                = Integer.parseInt(sharedPref.getString(keyPrefMaxBitrate, "20000"));
            target_drop = Integer.parseInt(sharedPref.getString(keyPrefTargetDrop, "30"));
            gccFallbackRTT
                = Integer.parseInt(sharedPref.getString(keyPrefGccFallbackRTT, "150"));
            gccFallbackDt
                = Integer.parseInt(sharedPref.getString(keyPrefGccFallbackDt, "10"));
            RTSetting.avgp
                = Float.parseFloat(sharedPref.getString(keyPrefDTAvgParam, "0.25"));
            minRTTParam
                = Float.parseFloat(sharedPref.getString(keyPrefMinRTTParam, "1.5"));
            Log.i("InferenceTF", "alpha : " + alpha + ", beta : " + beta + ", upper : " + upper + ", lower : " + lower);
        }

        @Override
        public int onUpdateBitrate(int adjustedBitrate) {
            if (connect_type == ConnectActivity.CONNECT_DEFAULT) {
                RTSetting.predictedAdjustedBitrate = adjustedBitrate;
                RTSetting.webrtcAdjustedBitrate = adjustedBitrate;
                return adjustedBitrate;
            } else if (connect_type == ConnectActivity.CONNECT_FBCC) {
                RTSetting.predictedAdjustedBitrate = adjustedBitrate;
                RTSetting.webrtcAdjustedBitrate = adjustedBitrate;
                return adjustedBitrate;
            }

            RTSetting.webrtcAdjustedBitrate = adjustedBitrate;
            if (RTSetting.predictedAdjustedBitrate > 0) {
                // adjustedBitrate = (int)(RTCInferService.predictedAdjustedBitrate);
                float dt = RTSetting.dt;
                if (dt <= d_target) {
                    adapt *= alpha;
                } else {
                    adapt *= beta;
                }
                if (adapt > upper) {
                    adapt = upper;
                } else if (adapt < lower) {
                    adapt = lower;
                }
                RTSetting.mismatchFixedBitrate
                    = (int)(RTSetting.predictedAdjustedBitrate * adapt);
                if (RTSetting.mismatchFixedBitrate > max_bitrate * 1000) {
                    RTSetting.mismatchFixedBitrate = max_bitrate * 1000;
                }
                // long rtt_bound = (RTSetting.min_rtt == 0) ? gccFallbackRTT : (long)(RTSetting.min_rtt * minRTTParam);
                // if (RTSetting.rtts[RTSetting.ridx] > rtt_bound && RTSetting.dt_avg < gccFallbackDt) {
                if (RTSetting.rtts[RTSetting.ridx]
                    > (RTSetting.min_rtt + RTSetting.dt_avg) * 1.2) {
                    Log.i("CallActivity", "mismatchFixedBitrate : " + RTSetting.mismatchFixedBitrate + ", adjustedBitrate : " + adjustedBitrate);
                    if (!fallbackFlag) setMMappedMessage("0,0,0,");
                    fallbackFlag = true;
                    RTSetting.gcc_fallback = 1;

                }
                // if (RTSetting.rtts[RTSetting.ridx] < rtt_bound && RTSetting.dt_avg > gccFallbackDt) {
                if (RTSetting.rtts[RTSetting.ridx]
                    < (RTSetting.min_rtt + RTSetting.dt_avg) * 1.05) {
                    if (fallbackFlag) setMMappedMessage("1111,0,0,");
                    fallbackFlag = false;
                    RTSetting.gcc_fallback = 0;
                }
                if (fallbackFlag) {
                    if (RTSetting.mismatchFixedBitrate < adjustedBitrate) {
                        // conClient.setBitrate(RTSetting.mismatchFixedBitrate);
                        return RTSetting.mismatchFixedBitrate;
                    } else {
                        RTSetting.mismatchFixedBitrate = adjustedBitrate;
                        // conClient.setBitrate(adjustedBitrate);
                        return adjustedBitrate;
                    }
                }
                adjustedBitrate = (int)(RTSetting.mismatchFixedBitrate);
                // conClient.setBitrate(adjustedBitrate);
                Log.i("CallActivity", "delta : " + adapt + ", dt : " + RTSetting.dt);
            }
            // long t = System.currentTimeMillis();
            // Log.i("CallActivity", "adjustedBitrate : " + adjustedBitrate + ", " + t);
            return adjustedBitrate;
        }

        public void setMMappedMessage(String msg) {
            if (mem != null) {
                mem.clear();
                mem.put(msg.getBytes());
            }
            Log.i("CallActivity", "=== null");
        }

        @Override
        public boolean onDropFrame() {
            frame_count++;
            try {
                if (frame_count % RTSetting.fps_adapt == 0 && RTSetting.fps_adapt < 30) {
                    frame_count = 0;
                    Log.i("CallActivity","drop!!");
                    return true;
                }
                if (frame_count > 10000) frame_count = 0;
            } catch(Exception e) {
                Log.i("CallActivity", "drop e : " + e.getMessage());
            }
            return false;
        }

        @Override
        public VideoFrame onFrameBeforeEncoding(VideoFrame frame) {
            // if (saver != null && frame_count % 40 == 1 && !remote) {
            //     int width = frame.getBuffer().toI420().getWidth();
            //     int height = frame.getBuffer().toI420().getHeight();
            //     // ((JavaI420Buffer)frame.getBuffer().toI420()).allocate(width, height);
            //     JavaI420Buffer buffer = JavaI420Buffer.allocate(width, height);
            //     frame.release();
            //     frame = new VideoFrame(buffer, frame.getRotation(), frame.getTimestampNs());
            //     ByteBuffer buf = frame.getBuffer().toI420().getDataY();
            //     Log.i("CallActivity", "===" + HexUtils.bytesToHex(HexUtils.getByteArrayFromByteBuffer(buf)));
            // }
            // if (saver != null && frame_count >= 0) {
            //     Log.i("CallActivity", "=== width : " + frame.getBuffer().getWidth() +", height : " + frame.getBuffer().getHeight());
            //     saver.onFrame(frame);
            //     frame_count++;
            // }
            return frame;
        }

        @Override
        public void onFrameAfterEncoding(VideoCodecStatus status, VideoFrame frame, long time) {
            // if (status == VideoCodecStatus.NO_OUTPUT) {
            //     // frame drop
            //     StatusDataCollector.saveFrameDrop(context);
            // }
            // InputInfoManager.encoding_time = time;
            // // Log.i("CallActivity", "[ENC] time : " + time + ", width : " + frame.getBuffer().getWidth() + ", height : " + frame.getBuffer().getHeight());

            if (!remote) {
                try {
                    VideoFrame.I420Buffer buf = frame.getBuffer().toI420();
                    ByteBuffer ybuffer = buf.getDataY();
                    byte[] barray = HexUtils.getByteArrayFromByteBuffer(ybuffer);
                    int index = HexUtils.getIndexFromBytes(barray);
                    Log.i("CallActivity", "[ENC] index : " + index);
                    StatusDataCollector.saveFrameIndex(context, remote, index);
                    buf.release();
                } catch(Exception e) {
                    Log.i("CallActivity", "e : " + e);
                }
            }
        }

        @Override
        public void setChannelParameters(short packetLoss, long roundTripTimeMs) {
            RTSetting.packetLoss = packetLoss;
            // RTSetting.roundTripTimeMs = roundTripTimeMs;
            RTSetting.ridx = RTSetting.next_ridx(RTSetting.ridx);
            RTSetting.rtts[RTSetting.ridx] = roundTripTimeMs;
            if (RTSetting.rtts[RTSetting.next_ridx(RTSetting.ridx)] != 0) {
                RTSetting.min_rtt = 5000;
                for (int i=0; i<10; i++) {
                    if (RTSetting.min_rtt > RTSetting.rtts[i]) {
                        RTSetting.min_rtt = RTSetting.rtts[i];
                    }
                }
            }
        }

        public void setFileSaver(VideoFileRenderer saver) {
            this.saver = saver;
        }

        public void setFrameCount(int count) {
            this.frame_count = count;
        }
        public int getFrameCount() {
            return frame_count;
        }

        public VideoFileRenderer getSaver() {
            return saver;
        }
    }

    private static class ProxyVideoSink implements VideoSink {

        private VideoSink target;
        private VideoFileRenderer saver;
        private int frame_count = 0;
        private Context context;

        public ProxyVideoSink(Context context) {
            this.context = context;
        }

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            target.onFrame(frame);
            if (saver != null && frame_count >= 0) {
                if (remote) {
                    VideoFrame.I420Buffer buf = frame.getBuffer().toI420();
                    ByteBuffer ybuffer = buf.getDataY();
                    byte[] barray = HexUtils.getByteArrayFromByteBuffer(ybuffer);
                    int index = HexUtils.getIndexFromBytes(barray);
                    StatusDataCollector.saveFrameIndex(context, remote, index);
                    buf.release();
                }
                // saver.onFrame(frame);
                // frame_count++;
            }
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }

        synchronized public void setFileSaver(VideoFileRenderer saver) {
            this.saver = saver;
        }

        synchronized public void setFrameCount(int count) {
            this.frame_count = count;
        }
        synchronized public int getFrameCount() {
            return frame_count;
        }

        synchronized public VideoFileRenderer getSaver() {
            return saver;
        }
    }

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink(CallActivity.this);
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink(CallActivity.this);
    @Nullable private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private SignalingParameters signalingParameters;
    @Nullable private AppRTCAudioManager audioManager;
    @Nullable
    private SurfaceViewRenderer pipRenderer;
    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private Toast logToast;
    private boolean commandLineRun;
    private boolean activityRunning;
    private RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    private boolean screencaptureEnabled;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;

    // Controls
    private CallFragment callFragment;
    private HudFragment hudFragment;
    private CpuMonitor cpuMonitor;

    @Override
    // TODO(bugs.webrtc.org/8580): LayoutParams.FLAG_TURN_SCREEN_ON and
    // LayoutParams.FLAG_SHOW_WHEN_LOCKED are deprecated.
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusDataCollector.clear();
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
                             | LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_call);

        remote = getIntent().getBooleanExtra(EXTRA_REMOTE, false);
        connect_type = getIntent().getIntExtra(EXTRA_CONNECT_TYPE,
                                          ConnectActivity.CONNECT_DEFAULT);

        iceConnected = false;
        signalingParameters = null;

        // Create UI controls.
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        callFragment = new CallFragment();
        hudFragment = new HudFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleCallControlFragmentVisibility();
                }
            };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSwappedFeeds(!isSwappedFeeds);
                }
            });

        fullscreenRenderer.setOnClickListener(listener);
        remoteSinks.add(remoteProxyRenderer);

        final Intent intent = getIntent();
        eglBase = EglBase.create(null, EglBase.CONFIG_PLAIN);

        // Create video renderers.
        pipRenderer.init(eglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        // String saveRemoteVideoToFile = intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
        // String saveRemoteVideoToFile = "/sdcard/Download/apprtc_receiver.y4m";
        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        // if (saveRemoteVideoToFile != null) {
        // if (remote) {
        //     int videoOutWidth = 1280;
        //     int videoOutHeight = 720;
        //     Log.i("CallActivity", "==== VideoFileRenderer Remote");
        //     // int videoOutWidth = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
        //     // int videoOutHeight = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
        //     try {
        //         videoFileRenderer = new VideoFileRenderer(
        //                                                   saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.getEglBaseContext());
        //         remoteSinks.add(videoFileRenderer);
        //         Log.i("CallActivity", "==== VideoFileRenderer added");
        //     } catch (IOException e) {
        //         throw new RuntimeException(
        //                                    "Failed to open video file for output: " + saveRemoteVideoToFile, e);
        //     }
        // }
        if (remote) {
            int videoOutWidth = targetWidth;
            int videoOutHeight = targetHeight;
            try {
                videoFileRenderer
                    = new VideoFileRenderer("/sdcard/Download/apprtc_receiver.y4m",
                                            videoOutWidth,
                                            videoOutHeight,
                                            eglBase.getEglBaseContext());
                // remoteSinks.add(videoFileRenderer);
                remoteProxyRenderer.setFileSaver(videoFileRenderer);
                remoteProxyRenderer.setFrameCount(0);
            } catch (IOException e) {
                throw new RuntimeException(
                                           "Failed to open video file for output: " + e);
            }
        }
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);



        Uri roomUri = intent.getData();
        if (roomUri == null) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Get Intent parameters.
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.d(TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);


        int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

        screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }

        DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                                                              intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                                                              intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                                                              intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }
        peerConnectionParameters =
            new PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                                         tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                                         intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                                         intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                                         intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                                         intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                                         intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                                         intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                                         intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                                         intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                                         intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                                         intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                                         intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                                         intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                                         intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false),
                                         intent.getBooleanExtra(EXTRA_USE_LEGACY_AUDIO_DEVICE, false), dataChannelParameters, remote);
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        int runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
        // Create connection parameters.
        String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
        roomConnectionParameters =
            new RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

        // Create CPU monitor
        if (CpuMonitor.isSupported()) {
            cpuMonitor = new CpuMonitor(this);
            hudFragment.setCpuMonitor(cpuMonitor);
        }

        // Send intent arguments to fragments.
        callFragment.setArguments(intent.getExtras());
        hudFragment.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.add(R.id.hud_fragment_container, hudFragment);
        ft.commit();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnect();
                    }
                }, runTimeMs);
        }

        // Create peer connection client.
        frameCallback = new EncoderFrameCallback(CallActivity.this);
        peerConnectionClient
            = new PeerConnectionClient(getApplicationContext(),
                                       eglBase,
                                       peerConnectionParameters,
                                       CallActivity.this,
                                       frameCallback);
        peerConnectionClient.setAudioEnabled(false);
        frameCallback.setPeerConnectionClient(peerConnectionClient);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }

        peerConnectionClient.createPeerConnectionFactory(options);
        if (screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }

        startInferService();
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
            (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
            (MediaProjectionManager) getApplication().getSystemService(
                                                                       Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                               mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private @Nullable VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    // ((CameraCapturer)videoCapturer).setCameraListener(new CameraCapturer.CameraListener() {
                    //         @Override
                    //         public void onStart(int width, int height, int framerate) {
                    //             if (remote) return;
                    //             try {
                    //                 videoFileRenderer
                    //                     = new VideoFileRenderer("/sdcard/Download/apprtc_sender.y4m",
                    //                                             width,
                    //                                             height,
                    //                                             eglBase.getEglBaseContext());
                    //                 Log.i("CallActivity", "=== width : " + width + ", height : " + height);
                    //                 // if (frameCallback != null)
                    //                 //     frameCallback.setFileSaver(videoFileRenderer);
                    //             } catch (IOException e) {
                    //                 e.printStackTrace();
                    //             }
                    //         }

                    //         @Override
                    //         public VideoFrame onFrameCaptured(VideoFrame frame) {
                    //             return frame;
                    //         }
                    //     });
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private @Nullable VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                                         mediaProjectionPermissionResultData, new MediaProjection.Callback() {
                                                 @Override
                                                 public void onStop() {
                                                     reportError("User revoked permission to capture the screen.");
                                                 }
                                             });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
        if (cpuMonitor != null) {
            cpuMonitor.pause();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
        if (cpuMonitor != null) {
            cpuMonitor.resume();
        }
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        stopInferService();
        super.onDestroy();
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoScalingSwitch(ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            // peerConnectionClient.setAudioEnabled(micEnabled);
            peerConnectionClient.setAudioEnabled(false);
        }
        return micEnabled;
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected || !callFragment.isAdded()) {
            return;
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (callControlFragmentVisible) {
            ft.show(callFragment);
            ft.show(hudFragment);
        } else {
            ft.hide(callFragment);
            ft.hide(hudFragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AudioManagerEvents() {
                // This method will be called each time the number of available audio
                // devices has changed.
                @Override
                public void onAudioDeviceChanged(
                                                 AudioDevice audioDevice, Set<AudioDevice> availableAudioDevices) {
                    onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
                }
            });
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
                                              final AudioDevice device, final Set<AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
              + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                .setTitle(getText(R.string.channel_error_title))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                                  new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int id) {
                                          dialog.cancel();
                                          disconnect();
                                      }
                                  })
                .create()
                .show();
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isError) {
                        isError = true;
                        disconnectWithErrorMessage(description);
                    }
                }
            });
    }

    private @Nullable VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        // String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        String videoFileAsCamera = "/sdcard/Download/apprtc_sender.y4m";
        if (!remote && videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        logAndToast("Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient
            .createPeerConnection(localProxyVideoSink,
                                  remoteSinks,
                                  videoCapturer,
                                  signalingParameters);

        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final SignalingParameters params) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectedToRoomInternal(params);
                }
            });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                        return;
                    }
                    logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                    peerConnectionClient.setRemoteDescription(sdp);
                    if (!signalingParameters.initiator) {
                        logAndToast("Creating ANSWER...");
                        // Create answer. Answer SDP will be sent to offering client in
                        // PeerConnectionEvents.onLocalDescription event.
                        peerConnectionClient.createAnswer();
                    }
                }
            });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                        return;
                    }
                    peerConnectionClient.addRemoteIceCandidate(candidate);
                }
            });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                        return;
                    }
                    peerConnectionClient.removeRemoteIceCandidates(candidates);
                }
            });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAndToast("Remote end hung up; dropping PeerConnection");
                    disconnect();
                }
            });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                        if (signalingParameters.initiator) {
                            appRtcClient.sendOfferSdp(sdp);
                        } else {
                            appRtcClient.sendAnswerSdp(sdp);
                        }
                    }
                    if (peerConnectionParameters.videoMaxBitrate > 0) {
                        Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                        peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                    }
                }
            });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        appRtcClient.sendLocalIceCandidate(candidate);
                    }
                }
            });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        appRtcClient.sendLocalIceCandidateRemovals(candidates);
                    }
                }
            });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        localProxyVideoSink.setFrameCount(0);
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAndToast("ICE connected, delay=" + delta + "ms");
                    iceConnected = true;
                    callConnected();
                }
            });
    }

    @Override
    public void onIceDisconnected() {
        localProxyVideoSink.setFrameCount(-1);
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAndToast("ICE disconnected");
                    iceConnected = false;
                    disconnect();
                }
            });
    }

    @Override
    public void onPeerConnectionClosed() {}

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isError && iceConnected) {
                        hudFragment.updateEncoderStatistics(reports);
                    }
                }
            });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

    private void startInferService() {
        // if (connect_type == ConnectActivity.CONNECT_DEFAULT) return;
        // Intent intent = new Intent(CallActivity.this, RTCInferService.class);
        if (remote) return;
        Intent intent = new Intent(CallActivity.this, MobileInsightService.class);
        intent.putExtra(EXTRA_CONNECT_TYPE, connect_type);
        intent.setAction(RTCInferService.ACTION_CONNECT);
        startService(intent);
    }

    private void stopInferService() {
        // if (connect_type == ConnectActivity.CONNECT_DEFAULT) return;
        // Intent intent = new Intent(CallActivity.this, RTCInferService.class);
        if (remote) return;
        Intent intent = new Intent(CallActivity.this, MobileInsightService.class);
        intent.setAction(RTCInferService.ACTION_DISCONNECT);
        stopService(intent);
    }

}
