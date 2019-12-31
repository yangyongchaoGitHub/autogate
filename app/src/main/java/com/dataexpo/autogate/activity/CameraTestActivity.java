package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.FaceService;
import com.dataexpo.autogate.service.MainApplication;

public class CameraTestActivity extends BascActivity implements OnFrameCallback {
    private static final String TAG = CameraTestActivity.class.getSimpleName();
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
        //Log.i(TAG, "image length :" + image.length);
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_camera.setImageBitmap(finalBitmap);
            }
        });

//        FaceSDKManager.getInstance().onBitmapDetectCheck(finalBitmap, null, null,
//                0, 0, 1, 3, new FaceDetectCallBack() {
//                    @Override
//                    public void onFaceDetectCallback(LivenessModel livenessModel) {
//                        // 输出结果
//                        checkCloseResult(livenessModel);
//                    }
//
//                    @Override
//                    public void onTip(int code, String msg) {
//                        //displayTip(code, msg);
//                    }
//
//                    @Override
//                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
//                        //showFrame(livenessModel);
//                    }
//                });
    }

    private void checkCloseResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                User user = null;
                if (livenessModel != null && livenessModel.getFaceInfo() != null) {
                    user = livenessModel.getUser();
                    if (user != null) {
                        //是否是同一个用户在镜头前
                        final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
                        Log.i(TAG, " face responseData image path: " + FileUtils.getUserPic(user.image_name));

                        Log.i(TAG, "识别成功");


                    } else {
                        Log.i(TAG, "识别失败");
                    }
                } else {
                    //Log.i(TAG, "识别失败,未检测到人脸");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        //MainApplication.getInstance().getService().setFrameCallback(null);
        if (MainApplication.getInstance().getService()!= null) {
            MainApplication.getInstance().getService().deleteFrameCallback(this);
        }
        super.onDestroy();
    }
}
