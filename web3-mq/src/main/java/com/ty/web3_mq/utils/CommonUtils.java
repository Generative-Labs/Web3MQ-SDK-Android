package com.ty.web3_mq.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    public static String getDate(){
        Date date = new Date();
        String strDateFormat = "dd/MM/yyyy hh:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        return sdf.format(date);
    }

    public static byte[] appendPrefix(int categoryType,byte pbType, byte[] data){
        int length = data.length;
        byte[] new_data = new byte[length + 2];
        new_data[0] = Integer.valueOf(categoryType).byteValue();
        new_data[1] = pbType;
        System.arraycopy(data, 0, new_data, 2, length);
        return new_data;
    }
}
