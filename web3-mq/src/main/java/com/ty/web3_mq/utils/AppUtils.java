package com.ty.web3_mq.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Author: Shper
 * Version: V0.1 2017/4/17
 */
public class AppUtils {

    public static String getAppId(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
//        if (null == pm) {
//            return BuildConfig.APPLICATION_ID;
//        }

        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                return pi.packageName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int getVersionCode(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
//        if (null == pm) {
//            return BuildConfig.VERSION_CODE;
//        }

        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getVersionName(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
//        if (null == pm) {
//            return BuildConfig.VERSION_NAME;
//        }

        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                return pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getProcessName(@NonNull Context context) {
        // ???????????? ?????????????????? ??????
        BufferedReader mBufferedReader = null;
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != mBufferedReader) {
                try {
                    mBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // ?????? ActivityManager ??????
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == android.os.Process.myPid()) {
                return procInfo.processName;
            }
        }

        return null;
    }

    public static Application getApplicationContext() {
        try {
            Application application = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
            if (application != null) {
                return application;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Application application = (Application) Class.forName("android.app.AppGlobals")
                    .getMethod("getInitialApplication").invoke(null, (Object[]) null);
            if (application != null) {
                return application;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
