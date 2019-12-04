package com.dataexpo.autogate.service;

import android.app.Application;

public class MainApplication extends Application {
    private static MainApplication application;
    private MainService service;

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
}
