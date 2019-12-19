package com.dataexpo.autogate.activity.faceset;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.face.listener.SdkInitListener;
import com.dataexpo.autogate.face.manager.FaceSDKManager;

public class FaceAuthActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = FaceAuthActivity.class.getSimpleName();
    private Context mContext;
    private TextView tv_device_id;
    private EditText et_key;
    private FaceAuth faceAuth;
    private static final int AUTH_ONLINE = 0;
    private static final int AUTH_OFFLINE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_face_auth);
        initView();
        initData();
    }

    private void initData() {
        faceAuth = new FaceAuth();
        tv_device_id.setText(faceAuth.getDeviceId(mContext));
        final String licenseOnLineKey = PreferencesUtil.getString("activate_online_key", "");
        et_key.setText(licenseOnLineKey);
    }

    private void initView() {
        et_key = findViewById(R.id.et_face_auth_key);
        findViewById(R.id.btn_face_auth_back).setOnClickListener(this);
        findViewById(R.id.btn_face_auth_check_license).setOnClickListener(this);
        findViewById(R.id.btn_face_auth_confirm).setOnClickListener(this);
        findViewById(R.id.btn_face_auth_offline).setOnClickListener(this);
        tv_device_id = findViewById(R.id.tv_face_auth_device_id_value);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face_auth_back:
                finish();
                break;

            case R.id.btn_face_auth_check_license:
                String path = FileUitls.getSDPath();
                String sdCardDir = path + "/" + "License.zip";
                if (FileUitls.fileIsExists(sdCardDir)) {
                    //ToastUtils.toast(mContext, "读取到License.zip文件，文件地址为：" + sdCardDir);
                } else {
                    //ToastUtils.toast(mContext, "未查找到License.zip文件");
                }
                break;

            case R.id.btn_face_auth_confirm:
                initLicense(AUTH_ONLINE);
                break;

            case R.id.btn_face_auth_offline:
                initLicense(AUTH_OFFLINE);
                break;

                default:
        }
    }

    private void initLicense(int num) {
        if (num == AUTH_ONLINE) {
            String key = et_key.getText().toString().trim().toUpperCase();
            if (TextUtils.isEmpty(key)) {
                Toast.makeText(mContext, "请输入激活序列号!", Toast.LENGTH_SHORT).show();
                return;
            }
            faceAuth.initLicenseOnLine(mContext, key, new Callback() {
                @Override
                public void onResponse(final int code, final String response) {
                    //ToastUtils.toast(mContext, code + response);
                    if (code == 0) {
                        FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                            @Override
                            public void initStart() {

                            }

                            @Override
                            public void initLicenseSuccess() {
                                //ToastUtils.toast(mContext, "在线激活完成");
                            }

                            @Override
                            public void initLicenseFail(int errorCode, String msg) {
                                //ToastUtils.toast(mContext, errorCode + msg);

                            }

                            @Override
                            public void initModelSuccess() {

                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });
                        finish();
                    }
                }
            });
        } else {
            faceAuth.initLicenseOffLine(mContext, new Callback() {
                @Override
                public void onResponse(final int code, final String response) {
                    if (code == 0) {
                        FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                            @Override
                            public void initStart() {

                            }

                            @Override
                            public void initLicenseSuccess() {
                                //ToastUtils.toast(mContext, "离线激活完成");
                            }

                            @Override
                            public void initLicenseFail(int errorCode, String msg) {
                                //ToastUtils.toast(mContext, errorCode + msg);
                            }

                            @Override
                            public void initModelSuccess() {

                            }

                            @Override
                            public void initModelFail(int errorCode, String msg) {

                            }
                        });
                        finish();
                    } else {
                        //ToastUtils.toast(mContext, response);
                    }
                }
            });
        }
    }
}
