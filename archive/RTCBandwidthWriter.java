package org.appspot.apprtc.perceive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RTCBandwidthWriter {

    private static long prev_write_bandwidth = 0;

    private static void sendSigusrMessage() {
        try {
            Process process = Runtime.getRuntime().exec("su -c kill -SIGUSR2 $(ps -A | grep \"apprtc\" | awk '{print $2}')");
            process.waitFor();
        } catch (IOException e) {

            throw new RuntimeException(e);

        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        }
    }

    public static void writeBandwidth(long write_bandwidth) {
        if (prev_write_bandwidth == write_bandwidth) return;
        prev_write_bandwidth = write_bandwidth;
        FileWriter fileWriter = null;
        try {
            File file = new File("/sdcard/Wodnload/sigusr1.txt");
            fileWriter = new FileWriter(file, false);
            fileWriter.append("" + write_bandwidth);
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
        sendSigusrMessage();
    }


}
