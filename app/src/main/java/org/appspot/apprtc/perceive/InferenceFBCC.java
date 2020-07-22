package org.appspot.apprtc.perceive;

import android.util.Log;

public class InferenceFBCC implements InferenceMethod {

    private int time_frame_window = 9;
    private int continuous_increase = 1;
    private int bandwidth_count = 0;
    private int buffer_count = 0;
    private int buffer_count_packet = 0;
    private long end_time = 0;
    private long begin_time = 0;
    private int flag = 0;
    private int bandwidth_avg = 0;
    private int buffer_avg = 0;
    private int previous = 0;
    private int previous_bandwidth = 0;

    public InferenceFBCC() {
        Log.i("InferenceFBCC", "Congestion using the LTE");
    }

    @Override
    public int inferDynamics() {
        return 1;
    }

    @Override
    public float inferTBS() {
        return (125);
    }

    // @Override
    // public long infer(CellInfo info) {
    //     int write_bandwidth = 300000;
        // if (!info.check) return write_bandwidth;
        // if (info.type.equals("UL")) {
        //     Log.i("InferenceFBCC", "This is a uplink control packet\t" + bandwidth_count);
        //     if (bandwidth_count == 0) {
        //         if (flag == 1) {
        //             bandwidth_avg += info.tb;
        //             bandwidth_count++;
        //             flag = 0;
        //         }
        //     } else {
        //         bandwidth_avg += info.tb;
        //         bandwidth_count++;
        //     }
        // } else if (info.type.equals("Buffer")) {
        //     Log.i("InferenceFBCC", "This is a Buffer control packet\t" + bandwidth_count);
        //     int buffer_value = info.buffer_bytes;
        //     if (buffer_count_packet == 0) {
        //         flag = 1;
        //         begin_time=info.time;
        //         buffer_avg = buffer_value;
        //     } else if (buffer_count_packet == 1) {
        //         buffer_avg += buffer_value;
        //         buffer_avg /= 2;
        //     } else {
        //         if (buffer_count == 0) {
        //             begin_time = info.time;
        //             flag = 1;
        //         }
        //         buffer_avg = ((buffer_avg * buffer_count_packet) + buffer_value) / (buffer_count_packet + 1);
        //         if (buffer_count == time_frame_window) {
        //             if (buffer_value > buffer_avg
        //                 && continuous_increase==1
        //                 && buffer_value > previous) {
        //                 end_time = info.time;
        //                 float elapsed_secs = (end_time - begin_time)/0.4f;
        //                 Log.i("InferenceFBCC", "** !!!! CONGESTION DETECTED");
        //                 Log.i("InferenceFBCC", "-- Window time is \t" + elapsed_secs);
        //                 write_bandwidth = 300000;
        //                 if (bandwidth_avg != 0 && bandwidth_count != 0 && elapsed_secs < 4.0f) {
        //                     write_bandwidth=(int)(bandwidth_avg*8/elapsed_secs);
        //                 } else if (previous_bandwidth != 0 && bandwidth_count != 0 && elapsed_secs < 4.0f) {
        //                     write_bandwidth = (int)(previous_bandwidth * 8 / elapsed_secs);
        //                 } else {
        //                     write_bandwidth = 300000;
        //                 }
        //                 showWriteBandwidth(write_bandwidth, elapsed_secs);
        //                 return write_bandwidth;
        //             } else {
        //                 end_time = info.time;
        //                 float elapsed_secs = (end_time - begin_time)/0.4f;
        //                 Log.i("InferenceFBCC", "** CONGESTION NOT DETECTED");

        //                 if (bandwidth_count != 0 && elapsed_secs < 0.4) {
        //                     write_bandwidth = (int)((bandwidth_avg*8)/(elapsed_secs));
        //                 }
        //                 showWriteBandwidth(write_bandwidth, elapsed_secs);
        //             }
        //             continuous_increase = 1;
        //             bandwidth_count = 0;
        //             buffer_count = -1;
        //             if (bandwidth_avg != 0) previous_bandwidth = bandwidth_avg;
        //         } else {
        //             if (buffer_value < previous) {
        //                 Log.i("InferenceFBCC", "-- Congestion will not happen as value is reduced\t");
        //                 Log.i("InferenceFBCC", "-- Current value\t" + buffer_value);
        //                 Log.i("InferenceFBCC", "-- Prev:\t" + previous);
        //             }
        //         }
        //     }
        //     buffer_count_packet++;
        //     buffer_count++;
        //     previous = buffer_value;
        // }
    //     return write_bandwidth;
    // }

    // public void showWriteBandwidth(int write_bandwidth, float elapsed_secs) {
    //     Log.i("InferenceFBCC", "-- The bandwidth that will be written to the file is\t" + write_bandwidth);
    //     Log.i("InferenceFBCC", "-- bandwidth summation of TX in the given window\t" + bandwidth_avg);
    //     Log.i("InferenceFBCC", "-- Window time\t" + elapsed_secs);
    //     Log.i("InferenceFBCC", "-- Total number of TX packets considered\t" + bandwidth_count);
    //     Log.i("InferenceFBCC", "-- The FBCC write bandwidth\t" + write_bandwidth);
    //     Log.i("InferenceFBCC", "-- Buffer avg:\t" + buffer_avg);
    //     Log.i("InferenceFBCC", "-- continuous_increase\t" + continuous_increase);
    // }

}
