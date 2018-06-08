package com.example.pratyush.ble_chat;

/**
 * Created by pratyush on 7/1/18.
 */

public class ByteUtils {
    public static byte[] reverse(byte[] value) {
        int length = value.length;
        byte[] reversed = new byte[length];
        for (int i = 0; i < length; i++) {
            reversed[i] = value[length - (i + 1)];
        }
        return reversed;
    }
}
