package com.dataexpo.autogate.activity.faceset;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.BaseConfig;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.face.utils.ConfigUtils;

public class MirrorSettingActivity extends BascActivity implements View.OnClickListener {
    private Switch sw_rgb;
    private Switch sw_nir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror_setting);
        initView();
        initData();
    }

    private void initView() {
        sw_nir = findViewById(R.id.sw_mirrir_setting_nir);
        sw_rgb = findViewById(R.id.sw_mirrir_setting_rgb);
        findViewById(R.id.btn_mirrir_setting_save).setOnClickListener(this);
    }

    private void initData() {
        if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == BaseConfig.MIRROR_POSITIVE) {  // rgb无镜像
            sw_rgb.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getMirrorRGB() == BaseConfig.MIRROR_REVERSE) {  // rgb有镜像
            sw_rgb.setChecked(true);
        }

        if (SingleBaseConfig.getBaseConfig().getMirrorNIR() == BaseConfig.MIRROR_POSITIVE) {  // nir无镜像
            sw_nir.setChecked(false);
        } else if (SingleBaseConfig.getBaseConfig().getMirrorNIR() == BaseConfig.MIRROR_REVERSE) {  // nir有镜像
            sw_nir.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mirrir_setting_save:
                if (sw_rgb.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setMirrorRGB(BaseConfig.MIRROR_REVERSE);
                } else {
                    SingleBaseConfig.getBaseConfig().setMirrorRGB(BaseConfig.MIRROR_POSITIVE);
                }

                if (sw_nir.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setMirrorNIR(BaseConfig.MIRROR_REVERSE);
                } else {
                    SingleBaseConfig.getBaseConfig().setMirrorNIR(BaseConfig.MIRROR_POSITIVE);
                }

                ConfigUtils.modityJson();
                FaceSDKManager.getInstance().initConfig();
                finish();
                break;
            default:
                break;
        }
    }
}
