package com.dataexpo.autogate.activity.faceset;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.face.utils.ConfigUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.dataexpo.autogate.face.model.BaseConfig.THRESHOLD_LIVENESS_MAX;
import static com.dataexpo.autogate.face.model.BaseConfig.THRESHOLD_LIVENESS_MIN;

public class FaceLivenessThresholdActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = FaceLivenessThresholdActivity.class.getSimpleName();
    private EditText et_rgb;
    private EditText et_nir;
    private EditText et_depth;
    private float rgb;
    private float nir;
    private float depth;
    private BigDecimal rgbDecimal;
    private BigDecimal nirDecimal;
    private BigDecimal depthDecimal;
    private BigDecimal nonmoralValue;
    private static final float templeValue = 0.01f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faceliveness_thrshold);
        initView();
        initData();
    }

    private void initData() {
        rgb = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        nir = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        depth = SingleBaseConfig.getBaseConfig().getDepthLiveScore();
        nonmoralValue = new BigDecimal(templeValue + "");
        et_rgb.setText(String.valueOf(rgb));
        et_nir.setText(String.valueOf(nir));
        et_depth.setText(String.valueOf(depth));
    }

    private void initView() {
        findViewById(R.id.btn_facelivenessthreshold_rgb_decrease).setOnClickListener(this);
        findViewById(R.id.btn_facelivenessthreshold_rgb_increase).setOnClickListener(this);
        findViewById(R.id.btn_facelivenessthreshold_nir_decrease).setOnClickListener(this);
        findViewById(R.id.btn_facelivenessthreshold_nir_increase).setOnClickListener(this);
        findViewById(R.id.btn_facelivenessthreshold_depth_decrease).setOnClickListener(this);
        findViewById(R.id.btn_facelivenessthreshold_depth_increase).setOnClickListener(this);
        et_rgb = findViewById(R.id.et_facelivenessthreshold_rgb);
        et_nir = findViewById(R.id.et_facelivenessthreshold_nir);
        et_depth = findViewById(R.id.et_facelivenessthreshold_depth);
        findViewById(R.id.btn_facelivenessthreshold_save).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_facelivenessthreshold_save:
                SingleBaseConfig.getBaseConfig().setRgbLiveScore(Float.parseFloat(et_rgb.getText().toString()));
                SingleBaseConfig.getBaseConfig().setNirLiveScore(Float.parseFloat(et_nir.getText().toString()));
                SingleBaseConfig.getBaseConfig().
                        setDepthLiveScore(Float.parseFloat(et_depth.getText().toString()));
                ConfigUtils.modityJson();
                FaceSDKManager.getInstance().initConfig();
                finish();
                break;

            case R.id.btn_facelivenessthreshold_rgb_decrease:
                if (rgb > THRESHOLD_LIVENESS_MIN && rgb <= THRESHOLD_LIVENESS_MAX) {
                    rgbDecimal = new BigDecimal(rgb + "");
                    rgb = rgbDecimal.subtract(nonmoralValue).floatValue();
                    et_rgb.setText(roundByScale(rgb));
                }
                break;

            case R.id.btn_facelivenessthreshold_rgb_increase:
                if (rgb > THRESHOLD_LIVENESS_MIN && rgb <= THRESHOLD_LIVENESS_MAX) {
                    rgbDecimal = new BigDecimal(rgb + "");
                    rgb = rgbDecimal.add(nonmoralValue).floatValue();
                    et_rgb.setText(roundByScale(rgb));
                }
                break;

            case R.id.btn_facelivenessthreshold_nir_decrease:
                if (nir > THRESHOLD_LIVENESS_MIN && nir <= THRESHOLD_LIVENESS_MAX) {
                    nirDecimal = new BigDecimal(nir + "");
                    nir = nirDecimal.subtract(nonmoralValue).floatValue();
                    et_nir.setText(roundByScale(nir));
                }
                break;

            case R.id.btn_facelivenessthreshold_nir_increase:
                if (nir > THRESHOLD_LIVENESS_MIN && nir <= THRESHOLD_LIVENESS_MAX) {
                    nirDecimal = new BigDecimal(nir + "");
                    nir = nirDecimal.add(nonmoralValue).floatValue();
                    et_nir.setText(roundByScale(nir));
                }
                break;

            case R.id.btn_facelivenessthreshold_depth_decrease:
                if (depth > THRESHOLD_LIVENESS_MIN && depth <= THRESHOLD_LIVENESS_MAX) {
                    depthDecimal = new BigDecimal(depth + "");
                    depth = depthDecimal.subtract(nonmoralValue).floatValue();
                    et_depth.setText(roundByScale(depth));
                }
                break;

            case R.id.btn_facelivenessthreshold_depth_increase:
                if (depth > THRESHOLD_LIVENESS_MIN && depth <= THRESHOLD_LIVENESS_MAX) {
                    depthDecimal = new BigDecimal(depth + "");
                    depth = depthDecimal.add(nonmoralValue).floatValue();
                    et_depth.setText(roundByScale(depth));
                }
                break;
                default:
        }
    }

    public static String roundByScale(float numberValue) {
        // 构造方法的字符格式这里如果小数不足2位,会以0补足.
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        // format 返回的是字符串
        String resultNumber = decimalFormat.format(numberValue);
        return resultNumber;
    }
}
