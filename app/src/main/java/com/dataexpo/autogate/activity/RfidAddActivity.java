package com.dataexpo.autogate.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.service.GateService;
import com.dataexpo.autogate.service.data.RfidService;

public class RfidAddActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = RfidAddActivity.class.getSimpleName();

    private EditText et_name;
    private EditText et_ip;
    private EditText et_port;
    private EditText et_remark;
    private TextView tv_back;
    private TextView tv_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_add);
        mContext = this;
        initView();
    }

    private void initView() {
        et_name = findViewById(R.id.tv_name);
        et_ip = findViewById(R.id.tv_ip);
        et_port = findViewById(R.id.tv_port);
        et_remark = findViewById(R.id.tv_remark);
        tv_back = findViewById(R.id.tv_back);
        tv_confirm = findViewById(R.id.tv_confirm);

        tv_back.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
            case R.id.ib_back:
                this.finish();
                break;

            case R.id.tv_confirm:
                Rfid rfid = new Rfid();
                rfid.setName(et_name.getText().toString());
                rfid.setIp(et_ip.getText().toString());
                rfid.setPort(et_port.getText().toString());
                rfid.setRemark(et_remark.getText().toString());
                RfidService.getInstance().insert(rfid);

                //通知通道门管理模块重启
                GateService.getInstance().restart();
                this.finish();
                break;

            default:
        }
    }

    //设置点击输入框外隐藏键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
