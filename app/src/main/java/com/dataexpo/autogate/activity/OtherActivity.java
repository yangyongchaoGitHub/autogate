package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;

import java.util.Vector;

import static com.dataexpo.autogate.service.GateService.*;
import static com.dataexpo.autogate.service.MQTTService.*;

public class OtherActivity extends BascActivity implements View.OnClickListener {
    private TextView tv_gate_status;
    private TextView tv_mqtt_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        mContext = this;
        initView();
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
            case R.id.btn_testcamera:
                startActivity(new Intent(mContext, CameraTestActivity.class));
                break;

            case R.id.btn_localip:
                startActivity(new Intent(mContext, IPActivity.class));
                break;

                default:
        }
    }
}
