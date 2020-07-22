package org.appspot.apprtc.perceive;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubPacketSample {

    @SerializedName("Sub FN")
    public String subFN;

    @SerializedName("Sys FN")
    public String sysFN;

    @SerializedName("LCIDs")
    public List<LCID> lcids;

    @SerializedName("Number of active LCID")
    public int numOfActiveLCID;
}
