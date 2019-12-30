package com.dataexpo.autogate.service;

import android.app.Application;

import static com.dataexpo.autogate.service.GateService.*;

public class MainApplication extends Application {
    private static MainApplication application;
    private MainService service;
    private int gateStatus = -1;
    private volatile int MQTTStatus = -1;
    private int faceSDKStatus;

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
}
