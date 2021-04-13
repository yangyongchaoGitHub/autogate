package com.dataexpo.autogate.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;

public class ClearDataActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = ClearDataActivity.class.getSimpleName();
    private TextView btn_back;
    private TextView tv_face_value;
    private TextView tv_record_value;
    private Button btn_clear_face_data;
    private Button btn_clear_gate_record;

    private ConstraintLayout using_cl;
    private ConstraintLayout login_cl;

    private EditText et_pswd;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_data);
        mContext = this;
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        using_cl.setVisibility(View.GONE);
        login_cl.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        et_pswd.setText("");
//        login_cl.setVisibility(View.GONE);
//        using_cl.setVisibility(View.VISIBLE);
        super.onPause();
    }

    private void initData() {
        //获取人像数据的总数
        int imgCount = UserService.getInstance().countOfImg();

        tv_face_value.setText(imgCount + "");

        int recordCount = CardService.getInstance().countOfTotalGate();
        tv_record_value.setText(recordCount + "");
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        tv_face_value = findViewById(R.id.tv_face_value);
        tv_record_value = findViewById(R.id.tv_record_value);
        btn_clear_face_data = findViewById(R.id.btn_clear_face_data);
        btn_clear_gate_record = findViewById(R.id.btn_clear_gate_record);

        btn_clear_face_data.setOnClickListener(this);
        btn_clear_gate_record.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        using_cl = findViewById(R.id.using_cl);
        login_cl = findViewById(R.id.login_cl);

        et_pswd = findViewById(R.id.et_pswd);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (et_pswd.getText().toString().trim().equals("140221")) {
                    login_cl.setVisibility(View.GONE);
                    using_cl.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(mContext, "验证失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_back:
                this.finish();
                break;

            case R.id.btn_clear_face_data:
                UserService.getInstance().clearAllImg();
                int imgCount = UserService.getInstance().countOfImg();
                tv_face_value.setText(imgCount + "");
                break;

            case R.id.btn_clear_gate_record:
                CardService.getInstance().clearAllData();
                int recordCount = CardService.getInstance().countOfTotalGate();
                tv_record_value.setText(recordCount + "");
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
