package org.appspot.apprtc.perceive;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class StatusDataCollector {

    private static final String TAG = "StatusDataCollector";
    public static String ex_id = "";

    public static boolean running = true;

    public static void clear() {
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/framedrop.csv");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/bitrate.csv");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/frame_index_receiver.csv");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/frame_index_sender.csv");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/cell_info.csv");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/webrtc_trace.txt");
        deleteFile("/sdcard/PerceiveAndroid/" + ex_id + "/mi-perceive.csv");
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        boolean deleted = file.delete();
    }

    public static void saveInfoLog(Context context, int aidx, int ridx) {
        String content = ""
            + MobileInsightService.Info.atime + ","
            + MobileInsightService.Info.mtime + ","
            + MobileInsightService.Info.rtime + ","
            + MobileInsightService.Info.ctime + ","
            + MobileInsightService.Info.tbs + ","
            + MobileInsightService.Info.rb + ","
            + MobileInsightService.Info.buffer_bytes + ","
            + aidx + ","
            + ridx
            + "\n";
        writeToFile(context, content, "mi-perceive.csv", "atime,mtime,rtime,ctime,tbs,rb,buffer_bytes,aidx,ridx\n");
    }

    public static void saveFrameDrop(Context context) {
        long ctime = System.currentTimeMillis();
        String content = "" + ctime + "\n";
        writeToFile(context, content, "framedrop.csv", "time\n");
    }

    public static void saveBitrate(Context context, long rtime, long ptime, int gtruth, int perceive, int mismatch, int webrtc, int fps, long enc_time, short packetLoss, long roundTripTimeMs, int buffer_bytes, float dt, float dt_avg, int gfallback, int fps_adapt) {
        String content = "" + rtime + "," + ptime + "," + gtruth + "," + perceive + "," + mismatch + "," + webrtc + "," + fps + "," + enc_time + "," + packetLoss + "," + roundTripTimeMs + "," + buffer_bytes + "," + dt + "," + dt_avg + "," + gfallback + "," + fps_adapt + "\n";
        writeToFile(context, content, "bitrate.csv", "time,ptime,gtruth,perceive,mismatch,webrtc,fps,enc_time,packet_loss,rtt,buffer_bytes,dt,dt_avg,fallback,fps_adapt\n");
    }

    public static void saveBitrate(Context context, int gtruth, int perceive, int webrtc, int fps, long enc_time, short packetLoss, long roundTripTimeMs, int buffer_bytes) {
        long ctime = System.currentTimeMillis();
        String content = "" + ctime + "," + gtruth + "," + perceive + "," + webrtc + "," + fps + "," + enc_time + "," + packetLoss + "," + roundTripTimeMs + "," + buffer_bytes + "\n";
        writeToFile(context, content, "bitrate.csv", "time,gtruth,perceive,webrtc,fps,enc_time,packet_loss,rtt,buffer_bytes\n");
    }

    public static void saveReceiverDelay(Context context, long mi_time, long cr_time) {
        String content = "" + mi_time + "," + cr_time + "\n";
        writeToFile(context, content, "receiver_delay.csv", "mi_time,cr_time\n");
    }


    public static void saveFrameIndex(Context context, boolean remote, int index) {
        String content = "" + System.currentTimeMillis() + "," + index + "\n";
        if (remote) {
            writeToFile(context, content, "frame_index_receiver.csv", "time,index\n");
        } else {
            writeToFile(context, content, "frame_index_sender.csv", "time,index\n");
        }
    }

    public static void saveCellInfo(Context context, String msg) {
        String content = msg + "\n";
        writeToFile(context, content, "cell_info.csv", "time,sfs,RSRP,RSRQ,CellId,RNTIID,nRBs,tb,mod,type,bandwidth,buffer_bytes,earfcn,arrival_time\n");
    }

    // public static void clearFrameTime(Context context) {
    //     String path = Environment.getExternalStorageDirectory().getPath();
    //     File file = new File(path + "/frame_time.csv");
    //     if (file.exists()) {
    //         boolean deleted = file.delete();
    //     }
    // }

    private static void writeToFile(Context context, String content, String fileName, String firstLine) {
        if (!running)
            return;

        FileWriter fileWriter = null;
        String path = Environment.getExternalStorageDirectory().getPath() + "/PerceiveAndroid/" + ex_id;

        File pathFile = new File(path);
        if (!pathFile.exists())
            if (!pathFile.mkdirs())
                Log.e(TAG, "CANNOT make " + pathFile.toString());

        // File file = new File(pathFile, fileName);
        boolean notFound = false;

        // String path = Environment.getExternalStorageDirectory().getPath();
        File file = new File(path + "/" + fileName);
        if (!file.exists()) {
            Log.i("StatusDataCollector", "Not found");
            notFound = true;
        }
        try {
            fileWriter = new FileWriter(file, true);
            if (notFound)
                fileWriter.append(firstLine);
            fileWriter.append(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
