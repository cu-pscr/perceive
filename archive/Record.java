package org.appspot.apprtc.perceive;

import com.google.gson.annotations.SerializedName;

public class Record {

    @SerializedName("Num of RB")
    public int numOfRB;

    @SerializedName("Current SFN SF")
    public int currSFNSF;

    @SerializedName("PUSCH TB Size")
    public int puschTBSize;

    @SerializedName("PUSCH Mod Order")
    public String puschModOrder;

}
