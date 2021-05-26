package com.dataexpo.autogate.service;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.company.NetSDK.NET_DEVICEINFO_Ex;
import com.dataexpo.autogate.activity.MainSelectActivity;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.service.Permissions;
import com.dataexpo.autogate.retrofitInf.URLs;
import com.dataexpo.autogate.service.keepalive.LocalService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.dataexpo.autogate.comm.Utils.LOCAL_RECORD_MODEL;
import static com.dataexpo.autogate.comm.Utils.MODEL_NAME;
import static com.dataexpo.autogate.comm.Utils.PERMISSION_ID;
import static com.dataexpo.autogate.comm.Utils.PERMISSION_NAME;

public class MainApplication extends Application {
    private final static String TAG = MainApplication.class.getSimpleName();
    public static int IMPORT_MQTT = 1;
    public static int IMPORT_LOCALFILE = 2;
    private static MainApplication application;
    private MainService service;
    private int gateStatus = -1;
    private volatile int MQTTStatus = -1;
    private int faceSDKStatus;
    private int importStatus = IMPORT_MQTT;

    //背景图片是否有变化
    private boolean bChange = false;

    private boolean bQueryImgEnd = false;
    //通道权限
    private Permissions permissions;
    //当前运行模式  0为普通模式，1为验证识别模式(按不同权限区别可进入区域) 默认是普通模式
    private int pModel = 0;

    //记录存储模式  0是离线模式   1是在线模式
    private int recordModel = 0;

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

        if (isMainProcess(getApplicationContext())) {
            Log.i(TAG,"------------ start");
            startService(new Intent(this, LocalService.class));
        }

        createRetrofit();
        String model = Utils.getModel(this, MODEL_NAME);
        if (model.equals("验证识别模式")) {
            pModel = 1;
            try {
                permissions = new Permissions();
                permissions.setId(Integer.parseInt(Utils.getModel(this, PERMISSION_ID)));
                permissions.setNames(Utils.getModel(this, PERMISSION_NAME));
            } catch (Exception e) {
                permissions = null;
            }
        }

        String recordStr = Utils.getLocal(this, LOCAL_RECORD_MODEL);
        if (!"".equals(recordStr)) {
            recordModel = Integer.parseInt(recordStr);
        }

        Log.i(TAG, "onCreate " + model + " " + permissions);
        if (permissions != null) {
            Log.i(TAG, "onCreate permissions: " + permissions.getId() + "  ||  " + permissions.getNames());
        }
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

    public int getpModel() {
        return pModel;
    }

    public void setpModel(int pModel) {
        this.pModel = pModel;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public boolean isbChange() {
        return bChange;
    }

    public void setbChange(boolean bChange) {
        this.bChange = bChange;
    }

    public int getRecordModel() {
        return recordModel;
    }

    public void setRecordModel(int recordModel) {
        this.recordModel = recordModel;
    }

    public boolean isbQueryImgEnd() {
        return bQueryImgEnd;
    }

    public void setbQueryImgEnd(boolean bQueryImgEnd) {
        this.bQueryImgEnd = bQueryImgEnd;
    }

    //双service保活
    public String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    public boolean isMainProcess(Context context) {
        Log.i(TAG,"------------ " + this.getApplicationContext().getPackageName() + " " + getCurrentProcessName(context));
        return context.getApplicationContext().getPackageName().equals(getCurrentProcessName(context));
    }

    private static MainSelectActivity mainActivity = null;

    public static MainSelectActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainSelectActivity mainActivity) {
        MainApplication.mainActivity = mainActivity;
    }
}
