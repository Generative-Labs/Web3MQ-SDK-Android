package com.ty.web3_mq.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

public class DateUtils {
    public static String getISOOffsetTime(long timeStamp){
        // 转换为OffsetDateTime对象
        Instant instant = Instant.ofEpochSecond(timeStamp);
        OffsetDateTime offsetDateTime = instant.atOffset(ZoneOffset.UTC);

        // 转换为指定格式的字符串
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return offsetDateTime.format(formatter);
    }

    public static long getTimeStampFromISOOffsetTime(String IOSOffset){
        // 解析为OffsetDateTime对象
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(IOSOffset, formatter);

        // 转换为时间戳（包含毫秒）
        Instant instant = offsetDateTime.toInstant();
        return instant.toEpochMilli();
    }


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
