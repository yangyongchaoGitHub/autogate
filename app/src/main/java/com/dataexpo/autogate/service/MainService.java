package com.dataexpo.autogate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;

import java.util.Vector;

import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;

public class MainService extends Service implements GateObserver {
    private static final String TAG = MainService.class.getSimpleName();
    private OnServeiceCallback callback;
    private MsgBinder mb = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (mb == null) {
            mb = new MsgBinder();
        }
        return mb;
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
    public void response(Vector<ReportData> mReports) {
        if (mReports != null && mReports.size() > 0) {
            for (ReportData data: mReports) {
                Log.i(TAG, "response card: " + data.getNumber() + " time " + data.getTime());
                User user = new User();
                user.cardCode = data.getNumber();

                User res = UserService.getInstance().findUserByCardCode(user);

                if (res != null) {
                    //有此用户
                    MainApplication.getInstance().getService().ledCtrl(LED_GREEN);

                } else {
                    MainApplication.getInstance().getService().ledCtrl(LED_RED);
                }
            }


        }
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

    public void setOnGateServiceCallback(OnGateServiceCallback callback) {
        GateService.getInstance().setCallback(callback);
    }

    public void testOption(byte b1, byte b2) {
        GateService.testOption(b1, b2);
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
    }

    @Override
    public void onDestroy() {
        MQTTService.getInstance().destroy();
        super.onDestroy();
    }
}
