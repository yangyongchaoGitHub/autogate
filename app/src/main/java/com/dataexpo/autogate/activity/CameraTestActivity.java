package com.dataexpo.autogate.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.service.MainApplication;

public class CameraTestActivity extends BascActivity implements OnFrameCallback {
    private static final String TAG = CameraTestActivity.class.getSimpleName();
    OnFrameCallback onFrameCallback = null;
    ImageView iv_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcamera);
        initView();
        MainApplication.getInstance().getService().setFrameCallback(this);
    }

    private void initView() {
        iv_camera = findViewById(R.id.iv_testcamera);
    }

    @Override
    public void onFrame(final byte[] image) {
        Log.i(TAG, "image length :" + image.length);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_camera.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        });

    }

    @Override
    protected void onDestroy() {
        MainApplication.getInstance().getService().setFrameCallback(null);
        super.onDestroy();
    }
}
