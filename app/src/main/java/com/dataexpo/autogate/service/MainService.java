package com.dataexpo.autogate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.netty.UDPClient;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;

import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;
import static com.dataexpo.autogate.service.MQTTService.MQTT_CONNECT_SUCCESS;

public class MainService extends Service implements GateObserver, MQTTObserver {
    private static final String TAG = MainService.class.getSimpleName();
    private OnServeiceCallback callback;
    private MsgBinder mb = null;
    UDPClient client = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (mb == null) {
            mb = new MsgBinder();
        }
        return mb;
    }

    public void setFrameCallback(OnFrameCallback onFrameCallback) {
        if (client != null) {
            client.setOnFrameCallback(onFrameCallback);
        }
    }

    public void deleteFrameCallback(OnFrameCallback onFrameCallback) {
        if (client != null) {
            client.deleteFrameCallback(onFrameCallback);
        }
    }

    public void setCallback(OnServeiceCallback onServeiceCallback) {
        this.callback = onServeiceCallback;
    }

//    public int getGateServiceStatus() {
//        return GateService.getInstance().getmStatus();
//    }

    public void restartGateService() {
        GateService.getInstance().restart();
    }

    public void restartMQTTService() {
        MQTTService.getInstance().restart();
    }

    @Override
    public void responseData(ReportData mReports) {
        if (mReports != null) {
            Log.i(TAG, "responseData card: " + mReports.getNumber() + " time " + mReports.getTime() + " " +
                    MainApplication.getInstance().getpModel() + " " + MainApplication.getInstance().getPermissions());
            User user = new User();
            user.cardCode = mReports.getNumber();

            User res = UserService.getInstance().findUserByCardCode(user);

            if (res != null) {
                //有此用户
                //检查权限：
                if (MainApplication.getInstance().getpModel() == 0) {
                    //验证模式要进行权限验证, 普通模式直接存数据
                    ledCtrl(LED_GREEN, mReports.getRfid());
                }

            } else {
                ledCtrl(LED_RED, mReports.getRfid());
            }
        }
    }

    @Override
    public void responseStatus(Rfid rfid) {
        //Log.i(TAG, "responseStatus: " + status);
        //MainApplication.getInstance().setGateStatus(status);
    }

    @Override
    public void responseMQTTStatus(int status) {
        if (status != MQTT_CONNECT_SUCCESS) {
            Log.i(TAG, "responseMQTTStatus: " + status);
        }
        MainApplication.getInstance().setMQTTStatus(status);
    }

    public class MsgBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public void addGateObserver(GateObserver observer) {
        GateService.getInstance().add(observer);
    }

    public void removeGateObserver(GateObserver observer) {
        GateService.getInstance().remove(observer);
    }

    public void addMQTTObserver(MQTTObserver observer) {
        MQTTService.getInstance().add(observer);
    }

    public void removeMQTTObserver(MQTTObserver observer) {
        MQTTService.getInstance().remove(observer);
    }

    public void setOnGateServiceCallback(OnGateServiceCallback callback) {
        GateService.getInstance().setCallback(callback);
    }

    public void ledCtrl(int target, Rfid rfid) {
        GateService.getInstance().ledCtrl(target, rfid);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化通道门服务
        GateService.getInstance().setContext(this);
        GateService.getInstance().start();


        addGateObserver(CardService.getInstance());
        addGateObserver(this);

//        MQTTHiveMQService.getInstance().init(this);
//        MQTTHiveMQService.getInstance().add(this);
//        MQTTService.getInstance().init(this);
//        MQTTService.getInstance().add(this);
        client= new UDPClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
