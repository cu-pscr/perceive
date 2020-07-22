package org.appspot.apprtc.perceive;

import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CellularInfo {

    private static long numbytes_sum = 0;

    @SerializedName("type_id")
    public String typeId;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("MCS 0")
    public String dl_mod;

    @SerializedName("TBS 0")
    public float tbs0;

    @SerializedName("TBS 1")
    public float tbs1;

    @SerializedName("Serving Cell ID")
    public int CellID;

    @SerializedName("PDSCH RNTIl ID")
    public int RntiID;

    @SerializedName("Number of Records")
    public int numRecords;

    @SerializedName("Subpackets")
    public List<SubPacket> subPackets;

    @SerializedName("RB Allocation Slot 0[0]")
    public String hexRB0;

    @SerializedName("RB Allocation Slot 0[1]")
    public String hexRB1;

    @SerializedName("Records")
    public List<Record> records;

    @SerializedName("Downlink bandwidth")
    public long dl_bandwidth;

    @SerializedName("Uplink bandwidth")
    public long ul_bandwidth;

    @SerializedName("Uplink frequency")
    public int ul_frequency;

    @SerializedName("Downlink frequency")
    public int dl_frequency;

    @SerializedName("Sys FN")
    public int sysFN;

    @SerializedName("Sub FN")
    public int subFN;

    public float get_vol_dl() {
        return (tbs0 + tbs1) / 8.0f;
    }

    public float get_rsrp() {
        if (subPackets != null && subPackets.size() > 0) {
            return subPackets.get(0).RSRP;
        }
        return 0.0f;
    }

    public float get_rsrq() {
        if (subPackets != null && subPackets.size() > 0) {
            return subPackets.get(0).RSRQ;
        }
        return 0.0f;
    }

    public float get_sfs() {
        if (subPackets != null && subPackets.size() > 0) {
            return subPackets.get(0).csfn * 10 + subPackets.get(0).csn;
        }
        return 0.0f;
    }

    public int get_nRbs() {
        if (TextUtils.isEmpty(hexRB0) || TextUtils.isEmpty(hexRB1))
            return 0;
        int nRB0 = HexUtils.countOne(hexRB0);
        int nRB1 = HexUtils.countOne(hexRB1);
        int nRbs = nRB0;
        if (nRbs < 1) nRbs = nRB1;
        if (nRbs < 1) nRbs = numRecords;
        return nRbs;
    }

    public boolean isDownlink() {
        if (typeId != null && typeId.equals("LTE_PHY_PDSCH_Packet"))
            return true;
        return false;
    }

    public boolean isServInfo() {
        if (typeId != null && typeId.equals("LTE_PHY_Serv_Cell_Measurement"))
            return true;
        return false;
    }

    public boolean isRRCInfo() {
        if (typeId != null && typeId.equals("LTE_RRC_Serv_Cell_Info"))
            return true;
        return false;
    }

    public long get_numbytes_sum() {
        if (typeId != null && typeId.equals("LTE_MAC_UL_Buffer_Status_Internal")) {
            numbytes_sum = 0;
            for (SubPacket pkt : subPackets) {

            }
        }
        return numbytes_sum;
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public String toString() {
        if (TextUtils.isEmpty(timestamp)) return "";
        String ret = "";
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(timestamp);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        // ret = "\ntime,sfs,RSRP,RSRQ,CellId,RNTIID,nRBs,tb,mod,type,bandwidth,buffer_bytes,earfcn\n";
        // ret += timeInMilliseconds;
        if (numRecords > 0) {
            // ret += "==== Uplink\n";
            for (Record record : records) {
                ret += timeInMilliseconds + ",";
                ret += record.currSFNSF + ",";
                ret += get_rsrp() + ",";
                ret += get_rsrq() + ",";
                ret += CellID + ",";
                ret += RntiID + ",";
                ret += record.numOfRB + ",";
                ret += record.puschTBSize + ",";
                ret += record.puschModOrder + ",";
                ret += "UL,";
                ret += ul_bandwidth + ",";
                if (subPackets != null && subPackets.size() > 0)
                    ret += subPackets.get(0).getNumBytesSum() + ",";
                else
                    ret += "0,";
                if (subPackets != null && subPackets.size() > 0)
                    ret += subPackets.get(0).earfcn;
                else
                    ret += "0";
                ret += "\n";
            }
        } else if (isDownlink() || isServInfo()) {
            // ret += "==== Downlink\n";
            ret += timeInMilliseconds + ",";
            ret += get_sfs() + ",";
            ret += get_rsrp() + ",";
            ret += get_rsrq() + ",";
            ret += CellID + ",";
            ret += RntiID + ",";
            ret += get_nRbs() + ",";
            ret += get_vol_dl() + ",";
            ret += dl_mod + ",";
            ret += "DL,";
            ret += dl_bandwidth + ",";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).getNumBytesSum() + ",";
            else
                ret += "0,";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).earfcn;
            else
                ret += "0";
            ret += "\n";
        } else if (isServInfo()) {
            // ret += "==== ServInfo\n";
            ret += timeInMilliseconds + ",";
            ret += get_sfs() + ",";
            ret += get_rsrp() + ",";
            ret += get_rsrq() + ",";
            ret += CellID + ",";
            ret += RntiID + ",";
            ret += get_nRbs() + ",";
            ret += get_vol_dl() + ",";
            ret += dl_mod + ",";
            ret += "ServInfo,";
            ret += dl_bandwidth + ",";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).getNumBytesSum() + ",";
            else
                ret += "0,";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).earfcn;
            else
                ret += "0";
            ret += "\n";
        } else if (isRRCInfo()) {
            // ret += "==== RRCInfo\n";
            ret += timeInMilliseconds + ",";
            ret += "NA,";
            ret += get_rsrp() + ",";
            ret += get_rsrq() + ",";
            ret += CellID + ",";
            ret += RntiID + ",";
            ret += get_nRbs() + ",";
            ret += get_vol_dl() + ",";
            ret += dl_mod + ",";
            ret += "RRC,";
            ret += dl_bandwidth + ",";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).getNumBytesSum() + ",";
            else
                ret += "0,";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).earfcn;
            else
                ret += "0";
            ret += "\n";
        } else if (typeId.equals("LTE_MAC_UL_Buffer_Status_Internal")) {
            // ret += "==== Buffer\n";
            ret += timeInMilliseconds + ",";
            ret += sysFN + "" + subFN + ",";
            ret += get_rsrp() + ",";
            ret += get_rsrq() + ",";
            ret += CellID + ",";
            ret += RntiID + ",";
            ret += get_nRbs() + ",";
            ret += get_vol_dl() + ",";
            ret += dl_mod + ",";
            ret += "Buffer,";
            ret += dl_bandwidth + ",";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).getNumBytesSum() + ",";
            else
                ret += "0,";
            if (subPackets != null && subPackets.size() > 0)
                ret += subPackets.get(0).earfcn;
            else
                ret += "0";
            ret += "\n";
        } else {
            ret = "";
            return ret;
        }

        return ret;
    }

}
