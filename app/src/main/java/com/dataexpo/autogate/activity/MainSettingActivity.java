package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dataexpo.autogate.R;

public class MainSettingActivity extends BascActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);
        mContext = this;
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_main_setting_1).setOnClickListener(this);
        findViewById(R.id.btn_main_setting_2).setOnClickListener(this);
        findViewById(R.id.btn_main_setting_3).setOnClickListener(this);
        findViewById(R.id.btn_main_setting_4).setOnClickListener(this);
        findViewById(R.id.btn_main_setting_5).setOnClickListener(this);
        findViewById(R.id.btn_main_setting_6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_setting_1:
                startActivity(new Intent(mContext, GateSetActivity.class));
                break;

            case R.id.btn_main_setting_2:
                startActivity(new Intent(mContext, UsersActivity.class));
                break;

            case R.id.btn_main_setting_3:
                break;
            case R.id.btn_main_setting_4:
                break;
            case R.id.btn_main_setting_5:
                break;
            case R.id.btn_main_setting_6:
                break;
                default:
        }
    }
}
