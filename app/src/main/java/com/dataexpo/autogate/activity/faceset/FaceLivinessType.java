package com.dataexpo.autogate.activity.faceset;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.face.utils.ConfigUtils;

import static com.dataexpo.autogate.face.model.BaseConfig.TYPE_NO_LIVE;
import static com.dataexpo.autogate.face.model.BaseConfig.TYPE_RGBANDDEPTH_LIVE;
import static com.dataexpo.autogate.face.model.BaseConfig.TYPE_RGBANDNIR_LIVE;
import static com.dataexpo.autogate.face.model.BaseConfig.TYPE_RGB_LIVE;

public class FaceLivinessType extends BascActivity implements View.OnClickListener {
    private static final String TAG = FaceLivinessType.class.getSimpleName();
    private RadioButton rb_no_live;
    private RadioButton rb_rgb_live;
    private RadioButton rb_rgbandnir_live;
    private RadioButton rb_rgbanddepth_live;
    private int mType;
    private int cameraType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faceliviness);
        initView();
        initData();
    }

    private void initData() {
        mType = SingleBaseConfig.getBaseConfig().getType();
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
        if (mType == TYPE_NO_LIVE) {
            rb_no_live.setChecked(true);
        } else if (mType == TYPE_RGB_LIVE) {
            rb_rgb_live.setChecked(true);
        } else if (mType == TYPE_RGBANDNIR_LIVE) {
            rb_rgbandnir_live.setChecked(true);
        } else if (mType == TYPE_RGBANDDEPTH_LIVE) {
            rb_rgbanddepth_live.setChecked(true);
        }

        rb_no_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_NO_LIVE;
                rb_rgb_live.setChecked(false);
                rb_rgbandnir_live.setChecked(false);
                rb_rgbanddepth_live.setChecked(false);
            }
        });

        rb_rgb_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_RGB_LIVE;
                rb_no_live.setChecked(false);
                rb_rgbandnir_live.setChecked(false);
                rb_rgbanddepth_live.setChecked(false);
            }
        });

        rb_rgbandnir_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_RGBANDNIR_LIVE;
                rb_no_live.setChecked(false);
                rb_rgb_live.setChecked(false);
                rb_rgbanddepth_live.setChecked(false);
            }
        });

        rb_rgbanddepth_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = TYPE_RGBANDDEPTH_LIVE;
                rb_no_live.setChecked(false);
                rb_rgb_live.setChecked(false);
                rb_rgbandnir_live.setChecked(false);
            }
        });
    }

    private void initView() {
        findViewById(R.id.btn_faceliviness_save).setOnClickListener(this);
        rb_no_live = findViewById(R.id.rb_no_live);
        rb_rgb_live = findViewById(R.id.rb_rgb_live);
        rb_rgbandnir_live = findViewById(R.id.rb_rgbandnir_live);
        rb_rgbanddepth_live = findViewById(R.id.rb_rgbanddepth_live);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_faceliviness_save:
                justify();
                ConfigUtils.modityJson();
                finish();
                break;
                default:
        }
    }

    public void justify() {
        if (mType == TYPE_NO_LIVE) {
            SingleBaseConfig.getBaseConfig().setType(TYPE_NO_LIVE);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (mType == TYPE_RGB_LIVE) {
            SingleBaseConfig.getBaseConfig().setType(TYPE_RGB_LIVE);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (mType == TYPE_RGBANDNIR_LIVE) {
            SingleBaseConfig.getBaseConfig().setType(TYPE_RGBANDNIR_LIVE);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (mType == TYPE_RGBANDDEPTH_LIVE) {
            SingleBaseConfig.getBaseConfig().setType(TYPE_RGBANDDEPTH_LIVE);
        }

        //cameraSelect();
    }

//    public void cameraSelect() {
//        if (cameraType == zero) {
//            SingleBaseConfig.getBaseConfig().setCameraType(zero);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
//        }
//        if (cameraTypeValue == one) {
//            SingleBaseConfig.getBaseConfig().setCameraType(one);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
//        }
//        if (cameraTypeValue == two) {
//            SingleBaseConfig.getBaseConfig().setCameraType(two);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
//        }
//        if (cameraTypeValue == three) {
//            SingleBaseConfig.getBaseConfig().setCameraType(three);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
//        }
//        if (cameraTypeValue == four) {
//            SingleBaseConfig.getBaseConfig().setCameraType(four);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
//        }
//        if (cameraTypeValue == five) {
//            SingleBaseConfig.getBaseConfig().setCameraType(five);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
//        }
//        if (cameraTypeValue == six) {
//            SingleBaseConfig.getBaseConfig().setCameraType(six);
//
//            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
//            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
//            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
//            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
//        }
//    }
}
