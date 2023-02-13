package com.ty.common.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    public static int dp2px(Context ctx, float dp) {
        float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void copy(Context context,String data){
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", data);
        cm.setPrimaryClip(mClipData);
    }
}
