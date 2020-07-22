package org.appspot.apprtc.perceive;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubPacket {

    @SerializedName("RSRP")
    public float RSRP;

    @SerializedName("RSRQ")
    public float RSRQ;

    @SerializedName("E-ARFCN")
    public int earfcn;

    @SerializedName("Current SFN")
    public int csfn;

    @SerializedName("Current Subframe Number")
    public int csn;

    @SerializedName("Num Samples")
    public int numSamples;

    @SerializedName("Samples")
    public List<SubPacketSample> samples;

    public int getNumBytesSum() {
        int numbytes_sum = 0;
        int count = 0;
        if (samples == null) return 0;
        for (SubPacketSample s : samples) {
            int numbytes = 0;
            for (LCID lcid : s.lcids) {
                numbytes += lcid.totalBytes;
                count++;
            }
            if (count != 0)
                numbytes = numbytes / count;
            numbytes_sum += numbytes;
        }
        return numbytes_sum;
    }

}
