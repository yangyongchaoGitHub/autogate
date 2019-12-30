package com.dataexpo.autogate.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.EditText;

import com.dataexpo.autogate.R;

public class IPActivity extends BascActivity {
    private static final String TAG = IPActivity.class.getSimpleName();
    private EditText et_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showip);
        initView();
        initData();
    }

    private void initData() {
        String ip;
        ConnectivityManager conMann = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);
            et_ip.setText(ip);
            //System.out.println("wifi_ip地址为------"+ip);
        }
    }

    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    private void initView() {
        et_ip = findViewById(R.id.et_ip_value);
    }
}
