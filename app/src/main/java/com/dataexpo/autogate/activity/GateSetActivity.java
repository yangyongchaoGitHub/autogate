package com.dataexpo.autogate.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.OnGateServiceCallback;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;

import java.util.Vector;

import static com.dataexpo.autogate.service.GateService.CONFIG_DEFAULT_IP;
import static com.dataexpo.autogate.service.GateService.CONFIG_DEFAULT_PORT;
import static com.dataexpo.autogate.service.GateService.GATE_STATUS_INIT_NULL_CONTEXT;
import static com.dataexpo.autogate.service.GateService.GATE_STATUS_INIT_NULL_SETTING;
import static com.dataexpo.autogate.service.GateService.GATE_STATUS_INIT_OPEN_FAIL;
import static com.dataexpo.autogate.service.GateService.GATE_STATUS_RUN;
import static com.dataexpo.autogate.service.GateService.GATE_STATUS_START;

public class GateSetActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = GateSetActivity.class.getSimpleName();
    private Context mContext;
    private EditText et_ip;
    private EditText et_port;
    private TextView tv_status;
    private String ip;
    private String port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_set);
        mContext = this;

        initView();

        onGateServiceCallback = new OnGateServiceCallback() {
            @Override
            public void onCallback(final int status, Vector<ReportData> mReports) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setStatusText(status);
                    }
                });
            }
        };
        //MainApplication.getInstance().getService().setOnGateServiceCallback(onGateServiceCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ip = Utils.getConfig(mContext, CONFIG_DEFAULT_IP);
        port = Utils.getConfig(mContext, CONFIG_DEFAULT_PORT);
        et_ip.setText(ip);
        et_port.setText(port);
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().removeGateObserver(this);
        }
    }

    private void setStatusText(int status) {
        String str = "";
        if (status == GATE_STATUS_INIT_NULL_SETTING) {
            str = "设置的端口或者ip为空";
        } else if (status == GATE_STATUS_INIT_OPEN_FAIL) {
            str = "打开接口错误";
        } else if (status == GATE_STATUS_INIT_NULL_CONTEXT) {
            str = "上下文为空";
        } else if (status == GATE_STATUS_RUN) {
            str = "正常运行中";
        } else if (status == GATE_STATUS_START) {
            str = "接口open成功";
        }

        tv_status.setText(str);
    }

    private void initView() {
        findViewById(R.id.btn_gate_set_back).setOnClickListener(this);
        et_ip = findViewById(R.id.et_gate_set_ip);
        et_port = findViewById(R.id.et_gate_set_port);
        findViewById(R.id.btn_gate_set_confirm).setOnClickListener(this);
        tv_status = findViewById(R.id.tv_gate_set_status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gate_set_back:
                this.finish();
                break;

            case R.id.btn_gate_set_confirm:
                if (checkValue()) {
                    saveConfig();
                    if (MainApplication.getInstance().getService().getGateServiceStatus() == GATE_STATUS_RUN) {
                        MainApplication.getInstance().getService().restartGateService();
                    }
                }

                break;

                default:
        }
    }

    private void saveConfig() {
        Log.i(TAG, "saveConfig ip:" + ip + " port: " + port);

        Utils.saveConfig(mContext, CONFIG_DEFAULT_IP, ip);
        Utils.saveConfig(mContext, CONFIG_DEFAULT_PORT, port);
    }

    private boolean checkValue() {
        String ip = et_ip.getText().toString();
        String port = et_port.getText().toString();
        if ("".equals(ip) || "".equals(port)) {
            Toast.makeText(mContext, "请输入正确的ip和端口", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (ip.equals(this.ip) && port.equals(this.port)) {
            return false;
        }
        this.ip = ip;
        this.port = port;

        return true;
    }
}
