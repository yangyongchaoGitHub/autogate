package com.dataexpo.autogate.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;

import java.util.Vector;

import static com.dataexpo.autogate.service.GateService.*;

public class OtherActivity extends BascActivity {
    private TextView tv_gate_status;
    private TextView tv_mqtt_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
        }
        int status = MainApplication.getInstance().getGateStatus();
        responseStatus(status);
    }

    private void initView() {
        tv_gate_status = findViewById(R.id.tv_gate_status_value);
        tv_mqtt_status = findViewById(R.id.tv_mqtt_status_value);
    }

    @Override
    public void responseStatus(int status) {
        super.responseStatus(status);
        String statuValue = "";
        switch (status) {
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
}
