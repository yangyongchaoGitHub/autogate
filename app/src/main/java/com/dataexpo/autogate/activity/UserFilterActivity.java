package com.dataexpo.autogate.activity;

import android.os.Bundle;
import android.view.View;

import com.dataexpo.autogate.R;

public class UserFilterActivity extends BascActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_filter);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_user_filter_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_user_filter_confirm:
                setResult(1);
                finish();
                break;
                default:
        }
    }
}
