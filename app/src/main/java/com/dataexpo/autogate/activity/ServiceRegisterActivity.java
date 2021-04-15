package com.dataexpo.autogate.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.service.Device;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.comm.Utils.EXPO_ADDRESS;
import static com.dataexpo.autogate.comm.Utils.EXPO_NAME;
import static com.dataexpo.autogate.comm.Utils.GATE_IP;
import static com.dataexpo.autogate.comm.Utils.GATE_PORT;

public class ServiceRegisterActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = ServiceRegisterActivity.class.getSimpleName();
    private EditText tv_expo_name;
    private EditText tv_device_name;
    private EditText tv_address;
    private TextView tv_back;
    private ImageView ib_back;
    private TextView tv_confirm;

    private String expoName;
    private String address;

    private Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_register);
        mContext = this;

        mRetrofit = MainApplication.getmRetrofit();
        initView();
    }

    private void initView() {
        expoName = Utils.getEXPOConfig(mContext, EXPO_NAME);
        address = Utils.getEXPOConfig(mContext, EXPO_ADDRESS);

        tv_expo_name = findViewById(R.id.tv_expo_name);
        tv_address = findViewById(R.id.tv_expo_add);
        tv_back = findViewById(R.id.tv_back);
        tv_confirm = findViewById(R.id.tv_confirm);
        tv_device_name = findViewById(R.id.tv_expo_dname);

        tv_back.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);

        tv_expo_name.setText(expoName);
        tv_address.setText(address);

        findViewById(R.id.btn_go_rfid).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_rfid:
                //到rfid配置界面
                startActivity(new Intent(mContext, RfidActivity.class));
                break;

            case R.id.tv_back:
            case R.id.ib_back:
                this.finish();
                break;

            case R.id.tv_confirm:
                //保存配置
                expoName = tv_expo_name.getText().toString().trim();
                address = tv_address.getText().toString().trim();

                saveToService();

                Utils.saveEXPOConfig(mContext, EXPO_NAME, expoName);
                Utils.saveEXPOConfig(mContext, EXPO_ADDRESS, address);
                break;
            default:
        }
    }

    private void saveToService() {
        ApiService apiService = mRetrofit.create(ApiService.class);

        Device device = new Device();
        device.setSerialNumber(Utils.getSerialNumber());
        device.setName(tv_device_name.getText().toString());
        device.setAddress(tv_address.getText().toString());
        device.setType(0);
        device.setExpoName(tv_expo_name.getText().toString());

        device.setIp(Utils.getLocalIpAddress());
        Log.e(TAG, " sn: " + device.getSerialNumber() + " | name " + device.getName() + " | address " +
                device.getAddress() + " | expoName: " + device.getExpoName() + " | ip " + device.getIp());

        Call<NetResult<String>> call = apiService.saveDeviceConfig(device);

        call.enqueue(new Callback<NetResult<String>>() {
            @Override
            public void onResponse(Call<NetResult<String>> call, Response<NetResult<String>> response) {
                NetResult<String> result = response.body();
                if (result == null) {
                    return;
                }
                Log.i(TAG, "onResponse" + result.getErrmsg() + " ! " +
                        result.getErrcode() + " " + result.getData());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<NetResult<String>> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
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
