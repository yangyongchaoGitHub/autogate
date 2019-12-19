package com.dataexpo.autogate.activity.faceset;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.activity.BascActivity;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.SingleBaseConfig;
import com.dataexpo.autogate.face.utils.ConfigUtils;

import static com.dataexpo.autogate.face.model.BaseConfig.MIN_FACE_LEAST;
import static com.dataexpo.autogate.face.model.BaseConfig.MIN_FACE_MOST;
import static com.dataexpo.autogate.face.model.BaseConfig.MIN_FACE_STEP;

public class MinFaceActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = MinFaceActivity.class.getSimpleName();
    private int min_face;
    private EditText et_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_min_face);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        min_face = SingleBaseConfig.getBaseConfig().getMinimumFace();
        et_value.setText(String.valueOf(min_face));
    }

    private void initView() {
        findViewById(R.id.btn_min_face_increase).setOnClickListener(this);
        findViewById(R.id.btn_min_face_decrease).setOnClickListener(this);
        findViewById(R.id.btn_min_face_save).setOnClickListener(this);
        et_value = findViewById(R.id.et_min_face);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_min_face_save:
                SingleBaseConfig.getBaseConfig().setMinimumFace(Integer.valueOf(et_value.getText().toString()));
                ConfigUtils.modityJson();
                FaceSDKManager.getInstance().initConfig();
                finish();
                break;

            case R.id.btn_min_face_increase:
                if (min_face >= MIN_FACE_LEAST && min_face < MIN_FACE_MOST) {
                    min_face += MIN_FACE_STEP;
                    et_value.setText(String.valueOf(min_face));
                }
                break;
            case R.id.btn_min_face_decrease:
                if (min_face > MIN_FACE_LEAST && min_face <= MIN_FACE_MOST) {
                    min_face -= MIN_FACE_STEP;
                    et_value.setText(String.valueOf(min_face));
                }
                break;
                default:
        }
    }
}
