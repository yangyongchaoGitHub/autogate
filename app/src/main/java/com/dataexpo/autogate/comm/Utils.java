package com.dataexpo.autogate.comm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class Utils {

    public static final String EXTRA_EXPO_USRE = "extra_expo_user";

    public static final String EXTRA_RFID_INFO = "extra_rfid_info";

    //TODO: change config filename
    //public static String USER_XML = "user";
    public static String MQTT_XML = "mqtt";
    public static String GATE_XML = "gate";
    public static String EXPO_XML = "expo";
    public static String MODEL_XML = "model";

    public static String MQTT_HOST = "host";
    public static String MQTT_PORT = "port";
    public static String MQTT_NAME = "name";
    public static String MQTT_PSWD = "pswd";
    public static String MQTT_CLIENTID = "clientid";
    public static String MQTT_TOPIC = "topic";

    public static String GATE_PORT = "gate_port";
    public static String GATE_IP = "gate_ip";
    public static String GATE_DIRECTION_SET = "direction_set";

    public static String EXPO_NAME = "expo_name";
    public static String EXPO_ADDRESS = "expo_address";

    public static String MODEL_NAME = "model_name";
    public static String PERMISSION_ID = "permission_id";
    public static String PERMISSION_NAME = "permission_name";

    public synchronized static String getModel(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(MODEL_XML,
                Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public synchronized static void saveModel(Context context, String key, String val) {
        SharedPreferences preferences = context.getSharedPreferences(MODEL_XML,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public synchronized static String getEXPOConfig(Context context, String sKey) {
        SharedPreferences preferences = context.getSharedPreferences(EXPO_XML,
                Context.MODE_PRIVATE);
        return preferences.getString(sKey, "");
    }

    public synchronized static void saveEXPOConfig(Context context, String sKey, String val) {
        SharedPreferences preferences = context.getSharedPreferences(EXPO_XML,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sKey, val);
        editor.commit();
    }

    public synchronized static String getGATEConfig(Context context, String sKey) {
        SharedPreferences preferences = context.getSharedPreferences(GATE_XML,
                Context.MODE_PRIVATE);
        return preferences.getString(sKey, "");
    }

    public synchronized static void saveGATEConfig(Context context, String sKey, String val) {
        SharedPreferences preferences = context.getSharedPreferences(GATE_XML,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sKey, val);
        editor.commit();
    }

    public synchronized static String getMQTTConfig(Context context, String sKey) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_XML,
                Context.MODE_PRIVATE);
        return preferences.getString(sKey, "");
    }

    public synchronized static void saveMQTTConfig(Context context, String sKey, String val) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_XML,
                Context.MODE_PRIVATE);
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
        String result = "";
        try {
            Date date = new Date(timeStamp);
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            result = dateFormat.format(date);
        } catch (Exception e) {

        }

        return result;
    }

    public static String getVersionName(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "1.0.0";
    }

    /**
     * 获取当前时间的字符串
     */
    public static String timeNow() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormat.format(date);
    }

    public static String timeNowLong() {
        Date date = new Date();
        return String.valueOf(date.getTime());
    }

    /**
     * 获取当前时间的字符串
     */
    public static long timeNow_() {
        return new Date().getTime();
    }

    public static String getSerialNumber() {
        String serial = "";
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
                serial = Build.SERIAL;

            } else {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serial;

    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {

            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "获取出错";
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }
}
