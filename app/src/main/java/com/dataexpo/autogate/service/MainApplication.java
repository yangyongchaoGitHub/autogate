package com.dataexpo.autogate.service;

import android.app.Application;

import com.company.NetSDK.NET_DEVICEINFO_Ex;
import com.dataexpo.autogate.retrofitInf.URLs;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainApplication extends Application {
    public static int IMPORT_MQTT = 1;
    public static int IMPORT_LOCALFILE = 2;
    private static MainApplication application;
    private MainService service;
    private int gateStatus = -1;
    private volatile int MQTTStatus = -1;
    private int faceSDKStatus;
    private int importStatus = IMPORT_MQTT;

    //网络框架
    private static Retrofit mRetrofit;

    private long mloginHandle;
    private NET_DEVICEINFO_Ex mDeviceInfo;

    public MainService getService() {
        return service;
    }

    public void setService(MainService service) {
        this.service = service;
    }

    public static MainApplication getInstance() {
        return  application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        createRetrofit();
    }

    public long getLoginHandle() {
        return mloginHandle;
    }

    public void setLoginHandle(long loginHandle) {
        this.mloginHandle = loginHandle;
    }

    public NET_DEVICEINFO_Ex getDeviceInfo() {
        return mDeviceInfo;
    }

    public void setDeviceInfo(NET_DEVICEINFO_Ex mDeviceInfo) {
        this.mDeviceInfo = mDeviceInfo;
    }

    public int getGateStatus() {
        return gateStatus;
    }

    public void setGateStatus(int gateStatus) {
        this.gateStatus = gateStatus;
    }

    public int getMQTTStatus() {
        return MQTTStatus;
    }

    public void setMQTTStatus(int MQTTStatus) {
        this.MQTTStatus = MQTTStatus;
    }

    public int getFaceSDKStatus() {
        return faceSDKStatus;
    }

    public void setFaceSDKStatus(int faceSDKStatus) {
        this.faceSDKStatus = faceSDKStatus;
    }

    public int getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(int importStatus) {
        this.importStatus = importStatus;
    }

    public static Retrofit getmRetrofit() {
        return mRetrofit;
    }
    public static void createRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLs.baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()))
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .build();
    }
}
