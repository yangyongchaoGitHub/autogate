package com.dataexpo.autogate.comm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    //TODO: change config filename
    //public static String USER_XML = "user";
    public static String MQTT_XML = "mqtt";

    public static String MQTT_HOST = "host";
    public static String MQTT_PORT = "port";
    public static String MQTT_NAME = "name";
    public static String MQTT_PSWD = "pswd";
    public static String MQTT_CLIENTID = "clientid";
    public static String MQTT_TOPIC = "topic";
    private static Bitmap bitmap = null;

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

    public synchronized static String getMQTTConfig(Context context, String sKey) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_XML,
                Context.MODE_WORLD_READABLE);
        return preferences.getString(sKey, "");
    }

    public synchronized static void saveMQTTConfig(Context context, String sKey, String val) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_XML,
                Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sKey, val);
        editor.commit();
    }

    /**
     * 时间格式转化
     * @param timeStamp
     * @param pattern
     * @return
     */
    public static String formatTime(long timeStamp, String pattern) {
        Date date = new Date(timeStamp);
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }
}
