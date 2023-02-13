package com.ty.web3_mq.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DateUtils {
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        return sdf.format(new Date());
    }

    public static String getTimeString(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timestamp));
    }
    public static String getTimeStringM(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        return sdf.format(new Date(timestamp));
    }

    public static String getTimeStringH(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }

    public static String getTimeStringNotification(long timestamp){
        long gapTime = System.currentTimeMillis() - timestamp;
        if(gapTime<60*1000){
            return gapTime/1000 +"s ago";
        }else if(gapTime<60*60*1000){
            return gapTime/(60*1000) + "m ago";
        }else if(gapTime<60*60*24*1000){
            return gapTime/(60*60*1000) + "hours ago";
        }else if(gapTime<60*60*24*7*1000){
            return gapTime/(60*60*24*1000) + "days ago";
        }else if(gapTime< 60L *60*24*30*1000){
            return gapTime/(60*60*24*7*1000) + "weeks ago";
        }else if(gapTime< 60L *60*24*365*1000){
            return gapTime/(60L *60*24*30*1000) + "mouths ago";
        }else{
            return gapTime/(60L *60*24*365*1000) + "year ago";
        }
    }
}
