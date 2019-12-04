package com.dataexpo.autogate.comm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    public synchronized static void saveConfig(Context context, String sKey, String val) {
        SharedPreferences preferences = context.getSharedPreferences(sKey,
                Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sKey, val);
        editor.commit();
    }

    public synchronized static String getConfig(Context context, String sKey) {
        SharedPreferences preferences = context.getSharedPreferences(sKey,
                Context.MODE_WORLD_READABLE);
        return preferences.getString(sKey, "");
    }
}
