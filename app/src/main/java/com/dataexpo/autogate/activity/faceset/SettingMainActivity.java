package com.dataexpo.autogate.activity.faceset;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;

public class SettingMainActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = SettingMainActivity.class.getSimpleName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_main);
        mContext = this;
        initView();
    }

    private void initView() {
        findViewById(R.id.tv_setting_main_min_face).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_user_auth).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_camera_display_angle).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_faceliveness).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_recognize_model_threshold).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_recognize_model).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_face_detect_angle).setOnClickListener(this);
        findViewById(R.id.tv_setting_main_mirror_setting).setOnClickListener(this);
        findViewById(R.id.btn_setting_main_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_setting_main_mirror_setting:
                startActivity(new Intent(mContext, MirrorSettingActivity.class));
                break;

            case R.id.tv_setting_main_face_detect_angle:
                //人像识别角度设置
                startActivity(new Intent(mContext, FaceDetectAngleActivity.class));
                break;

            // 最小识别人脸像素设置
            case R.id.tv_setting_main_min_face:
                startActivity(new Intent(mContext, MinFaceActivity.class));
                break;

                //授权激活界面
            case R.id.tv_setting_main_user_auth:
                startActivity(new Intent(mContext, FaceAuthActivity.class));
                break;

            case R.id.btn_setting_main_back:
                finish();
                break;

            case R.id.tv_setting_main_camera_display_angle:
                // 摄像头视频流回显角度
                startActivity(new Intent(mContext, CameraDisplayAngleActivity.class));
                break;

                //镜头及活体模式设置
            case R.id.tv_setting_main_faceliveness:
                startActivity(new Intent(mContext, FaceLivinessType.class));
                break;

                //识别模型阈值
            case R.id.tv_setting_main_recognize_model_threshold:
                startActivity(new Intent(this, RecognizeModleThresholdActivity.class));
                break;

                //活体检测阈值
            case R.id.tv_setting_main_recognize_model:
                startActivity(new Intent(mContext, FaceLivenessThresholdActivity.class));
                break;

                default:
        }
    }
}
