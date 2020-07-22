package org.appspot.apprtc.perceive;

import java.util.StringTokenizer;

public class CellInfo {

    public static long time;
    public static int sfs;
    public static float rsrp;
    public static float rsrq;
    public static int cell_id;
    public static int rnti_id;
    public static int nRBs;
    public static int tbs;
    public static String mod;
    public static String type;
    public static int bandwidth;
    public static int buffer_bytes;
    public static int earfcn;

    public static void updateCellInfo(String msg) {
        StringTokenizer st = new StringTokenizer(msg, ",");
        int cnt = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            switch(cnt) {
            case 9:
                type = s;
                break;
            case 11:
                try {
                    buffer_bytes = Integer.parseInt(s);
                } catch (Exception e) {
                }
                break;
            case 6:
                try {
                    nRBs = Integer.parseInt(s);
                } catch (Exception e) {
                }
                break;
            case 7:
                try {
                    tbs = Integer.parseInt(s);
                } catch (Exception e) {
                }
                break;
            }
            cnt++;
        }
    }

    // public String toString() {
    //     String rst = "";
    //     rst += type + ",";
    //     rst += nRBs + ",";
    //     rst += tbs;
    //     return rst;
    // }

}
