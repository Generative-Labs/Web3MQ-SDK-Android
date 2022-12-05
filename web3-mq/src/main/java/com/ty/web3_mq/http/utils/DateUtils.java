package com.ty.web3_mq.http.utils;

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
}
