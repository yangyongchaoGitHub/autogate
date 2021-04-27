package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.service.MainApplication;

import static com.dataexpo.autogate.comm.Utils.LOCAL_RECORD_MODEL;

public class RecordModelActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = RecordModelActivity.class.getSimpleName();

    private ConstraintLayout btn_record_1;
    private ConstraintLayout btn_record_2;
    private TextView tv_record_1;
    private TextView tv_record_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_model);
        mContext = this;
        initView();
    }

    private void initView() {
        btn_record_1 = findViewById(R.id.btn_record_1);
        btn_record_2 = findViewById(R.id.btn_record_2);
        tv_record_1 = findViewById(R.id.tv_record_1);
        tv_record_2 = findViewById(R.id.tv_record_2);

        btn_record_1.setOnClickListener(this);
        btn_record_2.setOnClickListener(this);
        tv_record_1.setOnClickListener(this);
        tv_record_2.setOnClickListener(this);

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);

        String recordStr = Utils.getLocal(this, LOCAL_RECORD_MODEL);
        if ("".equals(recordStr)) {
            recordStr = "0";
        }
        int recordModel = Integer.parseInt(recordStr);
        if (0 == recordModel) {
            changeSelect(btn_record_1, btn_record_2, tv_record_1, tv_record_2);
        } else {
            changeSelect(btn_record_2, btn_record_1, tv_record_2, tv_record_1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record_1:
            case R.id.tv_record_1:
                Utils.saveLocal(mContext, LOCAL_RECORD_MODEL, "0");
                MainApplication.getInstance().setRecordModel(0);
                changeSelect(btn_record_1, btn_record_2, tv_record_1, tv_record_2);
                break;

            case R.id.btn_record_2:
            case R.id.tv_record_2:
                Utils.saveLocal(mContext, LOCAL_RECORD_MODEL, "1");
                MainApplication.getInstance().setRecordModel(1);
                changeSelect(btn_record_2, btn_record_1, tv_record_2, tv_record_1);
                break;

            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();
                break;
                default:
        }
    }

    private void changeSelect(ConstraintLayout cChoose, ConstraintLayout cNoChoose, TextView tChoose, TextView tNoChoose) {
        cChoose.setBackgroundResource(R.drawable.rect_dark_blue);
        cNoChoose.setBackground(null);
        tChoose.setTextColor(getResources().getColor(R.color.white));
        tNoChoose.setTextColor(getResources().getColor(R.color.black));
    }
}
