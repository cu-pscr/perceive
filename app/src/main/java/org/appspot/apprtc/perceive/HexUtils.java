package org.appspot.apprtc.perceive;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public class HexUtils {
    // Hex.substring(2)

    public static int countOne(String text) {
        int total_count = 0;
        text = text.substring(2);
        for (int i=0; i<text.length(); i+=2) {
            String temp = text.substring(i, i+2);
            total_count += countChar(hexToBinary(temp), '1');
        }
        return total_count;
    }

    public static String hexToBinary(String Hex) {
        int i = Integer.parseInt(Hex, 16);
        String Bin = Integer.toBinaryString(i);
        return Bin;
    }

    public static int countChar(String str, char c) {
        int count = 0;
        for(int i=0; i < str.length(); i++) {
            if(str.charAt(i) == c)
                count++;
        }
        return count;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void fillzero(final ByteBuffer buf) {
        int capacity = buf.capacity();
        int limit = buf.limit();
        int position = buf.position();
        for (int i=0; i<capacity; i++) {
            buf.position(i);
            buf.put((byte)0x00);
        }
        // buf.rewind();
    }

    public static byte[] intToByte(int a) {
        byte[] result = new byte[4];
        result[0] |= (byte)((a&0xFF000000)>>24);
        result[1] |= (byte)((a&0xFF0000)>>16);
        result[2] |= (byte)((a&0xFF00)>>8);
        result[3] |= (byte)(a&0xFF);
        return result;
    }

    public static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    public static int getIndexFromBytes(byte[] bytes) {
        int ret = 0;
        for (int i=0; i<15; i++) {
            int v = bytes[25 + 70 * i] & 0xFF;
            if (v > 20) {
                ret += 1 << i;
            }
        }
        return ret;
    }
}
