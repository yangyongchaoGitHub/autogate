package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dataexpo.autogate.R;

public class RecordsActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = RecordsActivity.class.getSimpleName();

    private TextView btn_gate_record_model;

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
        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
        findViewById(R.id.iv_record_1).setOnClickListener(this);
        findViewById(R.id.iv_record_1).setOnClickListener(this);
        findViewById(R.id.iv_record_2).setOnClickListener(this);
        findViewById(R.id.tv_record_2).setOnClickListener(this);

        btn_gate_record_model = findViewById(R.id.btn_gate_record_model);
        btn_gate_record_model.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gate_record_model:
                startActivity(new Intent(mContext, RecordModelActivity.class));
                break;

            case R.id.btn_record_1:
            case R.id.iv_record_1:
            case R.id.tv_record_1:
                startActivity(new Intent(mContext, FaceRecordActivity.class));
                break;

            case R.id.btn_record_2:
            case R.id.iv_record_2:
            case R.id.tv_record_2:
                startActivity(new Intent(mContext, GateRecordActivity.class));
                break;

            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();
                break;
                default:
        }
    }
}
