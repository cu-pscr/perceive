package org.appspot.apprtc.perceive;

import android.text.TextUtils;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CellularInfoWrapper {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public long time;
    public float sfs;
    public float rsrp;
    public float rsrq;
    public int cellId;
    public int rntiId;
    public int nRBs;
    public float tb;
    public String mod;
    public String type;
    public long bandwidth;
    public int buffer_bytes;
    public int earfcn;

    public boolean check = true;
    private CellularInfo info;

    public CellularInfoWrapper(CellularInfo info) {
        this.info = info;
        if (TextUtils.isEmpty(info.timestamp)) {
            check = false;
            return;
        }
        String ret = "";
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(info.timestamp);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            check = false;
            return;
        }
        // ret = "\time,sfs,RSRP,RSRQ,CellId,RNTIID,nRBs,tb,mod,type,bandwidth,buffer_bytes,earfcn\n";
        // ret += timeInMilliseconds;
        if (info.typeId.equals("LTE_MAC_UL_Buffer_Status_Internal")) {
            time = timeInMilliseconds;
            sfs = info.sysFN + info.subFN;
            rsrp = info.get_rsrp();
            rsrq = info.get_rsrq();
            cellId = info.CellID;
            rntiId = info.RntiID;
            nRBs = info.get_nRbs();
            tb = info.get_vol_dl();
            mod = info.dl_mod;
            type = "Buffer";
            bandwidth = info.ul_bandwidth;
            if (info.subPackets != null && info.subPackets.size() > 0)
                buffer_bytes = info.subPackets.get(0).getNumBytesSum();
            else
                buffer_bytes = 0;
            if (info.subPackets != null && info.subPackets.size() > 0)
                earfcn = info.subPackets.get(0).earfcn;
            else
                earfcn = 0;
        } else if (info.numRecords > 0) {
            // ret += "==== Uplink\n";
            for (Record record : info.records) {
                time = timeInMilliseconds;
                sfs = record.currSFNSF;
                rsrp = info.get_rsrp();
                rsrq = info.get_rsrq();
                cellId = info.CellID;
                rntiId = info.RntiID;
                nRBs = record.numOfRB;
                tb = record.puschTBSize;
                mod = record.puschModOrder;
                type = "UL";
                bandwidth = info.ul_bandwidth;
                if (info.subPackets != null && info.subPackets.size() > 0)
                    buffer_bytes = info.subPackets.get(0).getNumBytesSum();
                else
                    buffer_bytes = 0;
                if (info.subPackets != null && info.subPackets.size() > 0)
                    earfcn = info.subPackets.get(0).earfcn;
                else
                    earfcn = 0;
            }
        } else if (info.isDownlink() || info.isServInfo()) {
            // ret += "==== Downlink\n";
            time = timeInMilliseconds;
            sfs = info.get_sfs();
            rsrp = info.get_rsrp();
            rsrq = info.get_rsrq();
            cellId = info.CellID;
            rntiId = info.RntiID;
            nRBs = info.get_nRbs();
            tb = info.get_vol_dl();
            mod = info.dl_mod;
            type = "DL";
            bandwidth = info.ul_bandwidth;
            if (info.subPackets != null && info.subPackets.size() > 0)
                buffer_bytes = info.subPackets.get(0).getNumBytesSum();
            else
                buffer_bytes = 0;
            if (info.subPackets != null && info.subPackets.size() > 0)
                earfcn = info.subPackets.get(0).earfcn;
            else
                earfcn = 0;
        } else if (info.isServInfo()) {
            time = timeInMilliseconds;
            sfs = info.get_sfs();
            rsrp = info.get_rsrp();
            rsrq = info.get_rsrq();
            cellId = info.CellID;
            rntiId = info.RntiID;
            nRBs = info.get_nRbs();
            tb = info.get_vol_dl();
            mod = info.dl_mod;
            type = "ServInfo";
            bandwidth = info.ul_bandwidth;
            if (info.subPackets != null && info.subPackets.size() > 0)
                buffer_bytes = info.subPackets.get(0).getNumBytesSum();
            else
                buffer_bytes = 0;
            if (info.subPackets != null && info.subPackets.size() > 0)
                earfcn = info.subPackets.get(0).earfcn;
            else
                earfcn = 0;
        } else if (info.isRRCInfo()) {
            time = timeInMilliseconds;
            sfs = 0;
            rsrp = info.get_rsrp();
            rsrq = info.get_rsrq();
            cellId = info.CellID;
            rntiId = info.RntiID;
            nRBs = info.get_nRbs();
            tb = info.get_vol_dl();
            mod = info.dl_mod;
            type = "RRC";
            bandwidth = info.ul_bandwidth;
            if (info.subPackets != null && info.subPackets.size() > 0)
                buffer_bytes = info.subPackets.get(0).getNumBytesSum();
            else
                buffer_bytes = 0;
            if (info.subPackets != null && info.subPackets.size() > 0)
                earfcn = info.subPackets.get(0).earfcn;
            else
                earfcn = 0;
        } else {
            check = false;
        }
    }

    public String toString() {
        return "" + time + "," + sfs + "," + rsrp + "," + rsrq + "," + cellId + "," + rntiId + "," + nRBs + "," + tb + "," + mod + "," + type + "," + bandwidth + "," + buffer_bytes + "," + earfcn + "\n";
        // return info.typeId +"\n";
        // return info.typeId + " " + info.numRecords + "\n";
        // if (type != null && type.equals("Buffer")) {
        //     return info.hexRB0 + "     " + info.hexRB1 + "\n";
        // }
        // return "";
        // return info.hexRB0 + "     " + info.hexRB1 + "\n";
    }
}
