package com.dataexpo.autogate.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.model.service.Device;
import com.dataexpo.autogate.model.service.DeviceQueryResult;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;

import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.service.GateService.*;
import static com.dataexpo.autogate.service.MQTTService.*;

public class OtherActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = OtherActivity.class.getSimpleName();
    private TextView tv_gate_status;
    private TextView tv_mqtt_status;
    private TextView tv_ip;
    private TextView tv_connect_service;

    private Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        mContext = this;
        initView();

        mRetrofit = MainApplication.getmRetrofit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
            MainApplication.getInstance().getService().addMQTTObserver(this);
        }
        int status = MainApplication.getInstance().getGateStatus();
        //responseStatus(status);
        status = MainApplication.getInstance().getMQTTStatus();
        responseMQTTStatus(status);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().removeGateObserver(this);
            MainApplication.getInstance().getService().removeMQTTObserver(this);
        }
    }

    private void initView() {
        tv_gate_status = findViewById(R.id.tv_gate_status_value);
        tv_mqtt_status = findViewById(R.id.tv_mqtt_status_value);
        findViewById(R.id.btn_testcamera).setOnClickListener(this);
        findViewById(R.id.btn_localip).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
        findViewById(R.id.btn_connect_service).setOnClickListener(this);

        tv_ip = findViewById(R.id.tv_ip);
        tv_connect_service = findViewById(R.id.tv_connect_service);
    }

    private void queryService() {
        ApiService apiService = mRetrofit.create(ApiService.class);

        Call<DeviceQueryResult> call = apiService.queryRegistInfo(Utils.getSerialNumber());

        call.enqueue(new Callback<DeviceQueryResult>() {
            @Override
            public void onResponse(Call<DeviceQueryResult> call, Response<DeviceQueryResult> response) {
                DeviceQueryResult result = response.body();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            tv_connect_service.setText("设备未注册");
                            return;
                        } else {
                            tv_connect_service.setText("连接成功");
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<DeviceQueryResult> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_connect_service.setText("连接服务器失败");
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
    }

    @Override
    public void responseStatus(Rfid rfid) {
        String statuValue = "";
        switch (rfid.status) {
            case GATE_STATUS_INIT_NULL_SETTING:
                statuValue = "设置的端口或者ip为空";
                break;

            case GATE_STATUS_START:
                statuValue = "接口open成功";
                break;

            case GATE_STATUS_RUN:
                statuValue = "正常运行中";
                break;

            case GATE_STATUS_INIT_OPEN_FAIL:
                statuValue = "打开接口错误";
                break;

            case GATE_STATUS_INIT_NULL_CONTEXT:
                statuValue = "上下文为空";
                break;

            case GATE_STATUS_READ_ERROR:
                statuValue = "和设备通信失败";
                default:
        }

        final String finalStatuValue = statuValue;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_gate_status.setText(finalStatuValue);
            }
        });
    }

    @Override
    public void responseMQTTStatus(int status) {
        super.responseMQTTStatus(status);
        String statuValue = "";
        switch (status) {
            case MQTT_CONNECT_INIT:
                statuValue = "连接失败";
                break;

            case MQTT_CONNECT_ING:
                statuValue = "正在连接";
                break;

            case MQTT_CONNECT_SUCCESS:
                statuValue = "连接成功";
                break;

            default:
        }

        final String finalStatuValue = statuValue;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_mqtt_status.setText(finalStatuValue);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect_service:
                //连接服务器获取数据，借此测试连接状态
                queryService();
                break;

            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();
                break;

            case R.id.btn_testcamera:
                //startActivity(new Intent(mContext, CameraTestActivity.class));
                break;

            case R.id.btn_localip:
                //startActivity(new Intent(mContext, IPActivity.class));
                tv_ip.setText(Utils.getLocalIpAddress());

                break;
            default:
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
}
