package com.dataexpo.autogate.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.service.MainApplication;

import static com.dataexpo.autogate.comm.Utils.*;

public class MQTTSettingActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = MQTTSettingActivity.class.getSimpleName();
    private EditText et_server;
    private EditText et_port;
    private EditText et_name;
    private EditText et_pswd;
    private EditText et_clientid;
    private EditText et_topic;

    private String host = "";
    private String port = "";
    private String name = "";
    private String pswd = "";
    private String clientid = "";
    private String topic = "";

    private String host_ = "";
    private String port_ = "";
    private String name_ = "";
    private String pswd_ = "";
    private String clientid_ = "";
    private String topic_ = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_setting);
        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        host = Utils.getMQTTConfig(mContext, MQTT_HOST);
        port = Utils.getMQTTConfig(mContext, MQTT_PORT);
        name = Utils.getMQTTConfig(mContext, MQTT_NAME);
        pswd = Utils.getMQTTConfig(mContext, MQTT_PSWD);
        clientid = Utils.getMQTTConfig(mContext, MQTT_CLIENTID);
        topic = Utils.getMQTTConfig(mContext, MQTT_TOPIC);

        host_ = host;
        port_ = port;
        name_ = name;
        pswd_ = pswd;
        clientid_ = clientid;
        topic_ = topic;

        if ("".equals(host)) {
            host = "tcp://192.168.1.9";
            Utils.saveMQTTConfig(mContext, MQTT_HOST, host);
        }
        if ("".equals(port)) {
            port = "61616";
            Utils.saveMQTTConfig(mContext, MQTT_PORT, port);
        }
        if ("".equals(name)) {
            name = "admin";
            Utils.saveMQTTConfig(mContext, MQTT_NAME, name);
        }
        if ("".equals(pswd)) {
            pswd = "admin";
            Utils.saveMQTTConfig(mContext, MQTT_PSWD, pswd);
        }
        if ("".equals(clientid)) {
            clientid = android.os.Build.SERIAL;
            Utils.saveMQTTConfig(mContext, MQTT_CLIENTID, clientid);
        }

        et_server.setText(host);
        et_port.setText(port);
        et_name.setText(name);
        et_pswd.setText(pswd);
        et_clientid.setText(clientid);
        //et_topic.setText(topic);
    }

    private void initView() {
        findViewById(R.id.btn_mqtt_setting_save).setOnClickListener(this);
        findViewById(R.id.btn_mqtt_setting_back).setOnClickListener(this);

        et_server = findViewById(R.id.et_mqtt_setting_url_value);
        et_port = findViewById(R.id.et_mqtt_setting_port_value);
        et_name = findViewById(R.id.et_mqtt_setting_name_value);
        et_pswd = findViewById(R.id.et_mqtt_setting_pswd_value);
        et_clientid = findViewById(R.id.et_mqtt_setting_clientid_value);
        et_topic = findViewById(R.id.et_mqtt_setting_topic_value);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mqtt_setting_save:
                setConfig();
                break;

            case R.id.btn_mqtt_setting_back:
                finish();
                break;
                default:
        }
    }

    private void setConfig() {
        if (check()) {
            host_ = host;
            port_ = port;
            name_ = name;
            pswd_ = pswd;
            clientid_ = clientid;
            topic_ = topic;

            Utils.saveMQTTConfig(mContext, MQTT_HOST, host);
            Utils.saveMQTTConfig(mContext, MQTT_PORT, port);
            Utils.saveMQTTConfig(mContext, MQTT_NAME, name);
            Utils.saveMQTTConfig(mContext, MQTT_PSWD, pswd);
            Utils.saveMQTTConfig(mContext, MQTT_CLIENTID, clientid);
            Utils.saveMQTTConfig(mContext, MQTT_TOPIC, topic);

            MainApplication.getInstance().getService().restartMQTTService();
        }
    }

    private boolean check() {
        boolean result = false;
        host = et_server.getText().toString();
        port = et_port.getText().toString();
        name = et_name.getText().toString();
        pswd = et_pswd.getText().toString();
        clientid = et_clientid.getText().toString();
        topic = et_topic.getText().toString();

        //需要不为空
        if (!("".equals(host) || "".equals(port) || "".equals(name) ||
                "".equals(pswd) || "".equals(clientid) || "".equals(topic))) {
            //判断是否有修改
            result = !(host.equals(host_) && port.equals(port_) && name.equals(name_) &&
                    pswd.equals(pswd_) && clientid.equals(clientid_) && topic.equals(topic_));
        }

        //Log.i(TAG, "result: " + result);
        return result;
    }
}
