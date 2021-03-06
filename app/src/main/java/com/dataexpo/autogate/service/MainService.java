package com.dataexpo.autogate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.model.service.RecordVo;
import com.dataexpo.autogate.model.service.mp.MsgBean;
import com.dataexpo.autogate.netty.UDPClient;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.comm.Utils.EXPO_ADDRESS;
import static com.dataexpo.autogate.comm.Utils.EXPO_ID;
import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;
import static com.dataexpo.autogate.service.MQTTService.MQTT_CONNECT_SUCCESS;

public class MainService extends Service implements GateObserver, MQTTObserver {
    private static final String TAG = MainService.class.getSimpleName();
    private OnServeiceCallback callback;
    private MsgBinder mb = null;
    UDPClient client = null;

    private Retrofit mRetrofit;

    String serialNum = Utils.getSerialNumber();

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
        //检查权限：
        if (mReports != null && MainApplication.getInstance().getpModel() == 0) {
            Log.i(TAG, "responseData card: " + mReports.getNumber() + " time " + mReports.getTime() + " " +
                    MainApplication.getInstance().getpModel() + " " + MainApplication.getInstance().getPermissions());
            User user = new User();

            user.cardCode = mReports.getNumber();
            mReports.setModel(0);
            mReports.setTime(Utils.timeNowLong());

            User res = UserService.getInstance().findUserByCardCode(user);

            if (res != null) {
                //有此用户
                mReports.setPid(res.pid);
                mReports.setEucode(res.code);
                mReports.setStatus(2);
                ledCtrl(LED_GREEN, mReports.getRfid());

            } else {
                mReports.setStatus(1);
                //ledCtrl(LED_RED, mReports.getRfid());
            }
            if (MainApplication.getInstance().getRecordModel() == 1) {
                //在线模式，直接上传数据到服务器
                mpUpload(mReports);
            } else {
                CardService.getInstance().insert(mReports);
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
        Log.i(TAG, "led: --------- " + target);
        GateService.getInstance().ledCtrl(target, rfid);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化通道门服务
        GateService.getInstance().setContext(this);
        GateService.getInstance().start();


        //addGateObserver(CardService.getInstance());
        addGateObserver(this);

//        MQTTHiveMQService.getInstance().init(this);
//        MQTTHiveMQService.getInstance().add(this);
//        MQTTService.getInstance().init(this);
//        MQTTService.getInstance().add(this);
        client= new UDPClient();

        mRetrofit = MainApplication.getmRetrofit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //上传数据
    private void mpUpload(ReportData mReports) {
        ApiService apiService = mRetrofit.create(ApiService.class);

        RecordVo recordVo = new RecordVo(mReports.getEucode(),
                new Date(Long.parseLong(mReports.getTime())), serialNum,
                Integer.parseInt(Utils.getEXPOConfig(MainApplication.getInstance(), EXPO_ID)),
                Utils.getEXPOConfig(MainApplication.getInstance(), EXPO_ADDRESS), mReports.getNumber(),
                mReports.getPid() + "");

        Call<MsgBean<String>> call = apiService.mpUploadRecord(recordVo);

        call.enqueue(new Callback<MsgBean<String>>() {
            @Override
            public void onResponse(Call<MsgBean<String>> call, Response<MsgBean<String>> response) {
                Log.i(TAG, "onResponse" + response);

                MsgBean<String> result = response.body();

                if (result == null || result.code == null || !result.code.equals(200)) {
                    CardService.getInstance().insert(mReports);
                }
            }

            @Override
            public void onFailure(Call<MsgBean<String>> call, Throwable t) {
                Log.i(TAG, "onFailure" + t.toString());
                CardService.getInstance().insert(mReports);
            }
        });

        call.hashCode();
    }
}
