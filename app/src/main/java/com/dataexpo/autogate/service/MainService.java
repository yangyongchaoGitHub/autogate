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
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.netty.UDPClient;

import org.eclipse.paho.android.service.MqttService;


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

    public int getGateServiceStatus() {
        return GateService.getInstance().getmStatus();
    }

    public void restartGateService() {
        GateService.getInstance().restart();
    }

    public void restartMQTTService() {
        MQTTService.getInstance().restart();
    }


    @Override
    public void responseData(ReportData mReports) {
        if (mReports != null) {
            Log.i(TAG, "responseData card: " + mReports.getNumber() + " time " + mReports.getTime());
            User user = new User();
            user.cardCode = mReports.getNumber();

            User res = UserService.getInstance().findUserByCardCode(user);

            if (res != null) {
                //有此用户
                MainApplication.getInstance().getService().ledCtrl(LED_GREEN);

            } else {
                MainApplication.getInstance().getService().ledCtrl(LED_RED);
            }
        }
    }

    @Override
    public void responseStatus(int status) {
        Log.i(TAG, "responseStatus: " + status);
        MainApplication.getInstance().setGateStatus(status);
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

    public void testOption(byte b1, byte b2) {
        GateService.ledOption(b1, b2);
    }

    public void ledCtrl(int target) {
        GateService.getInstance().ledCtrl(target);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GateService.getInstance().start();
        GateService.getInstance().setContext(this);
        addGateObserver(CardService.getInstance());
        addGateObserver(this);

        MQTTService.getInstance().init(this);
        MQTTService.getInstance().add(this);
        client= new UDPClient();
    }

    @Override
    public void onDestroy() {
        MQTTService.getInstance().exit();
        Intent stop = new Intent(this, MqttService.class);
        stopService(stop);
        super.onDestroy();
    }
}
