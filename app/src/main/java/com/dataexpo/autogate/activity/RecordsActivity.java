package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dataexpo.autogate.R;

public class RecordsActivity extends BascActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        mContext = this;
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_record_1).setOnClickListener(this);
        findViewById(R.id.btn_record_2).setOnClickListener(this);
        findViewById(R.id.btn_record_3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record_1:
                startActivity(new Intent(mContext, FaceRecordActivity.class));

                break;
            case R.id.btn_record_2:
                startActivity(new Intent(mContext, GateRecordActivity.class));

                break;
            case R.id.btn_record_3:
                break;
                default:
        }
    }
}
