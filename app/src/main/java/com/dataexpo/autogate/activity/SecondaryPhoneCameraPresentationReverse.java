package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.NetSDK.CB_fSnapRev;
import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.dahuacameracommon.CapturePictureModule;
import com.dataexpo.autogate.dahuacameracommon.EncodeModule;
import com.dataexpo.autogate.dahuacameracommon.IPLoginModule;
import com.dataexpo.autogate.dahuacameracommon.LivePreviewModule;
import com.dataexpo.autogate.dahuacameracommon.NetSDKLib;
import com.dataexpo.autogate.dahuacameracommon.OSDModule;
import com.dataexpo.autogate.dahuacameracommon.PTZControl;
import com.dataexpo.autogate.face.callback.FaceDetectCallBack;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.face.manager.FaceSDKManager;
import com.dataexpo.autogate.face.manager.ImportFileManager;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.listener.FaceOnSecCallback;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.MainApplication;

import static com.dataexpo.autogate.model.User.AUTH_SUCCESS;

/**
 * 原分屏是小屏，现分屏是大屏，所以做次调整
 */
public class SecondaryPhoneCameraPresentationReverse extends Presentation implements View.OnClickListener {
    private static final String TAG = SecondaryPhoneCameraPresentationReverse.class.getSimpleName();
    private Context mContext;

    private ImageView iv_head;
    private ImageView iv_face;
    private TextView tv_name;
    private TextView tv_direction;
    private User faceCurrUser = null;
    private User GateCurrUser = null;

    private ServiceConnection mConnection;
    private AutoTexturePreviewView mAutoCameraPreviewView;

    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    public SecondaryPhoneCameraPresentationReverse(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate!! ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);

        initView();

    }

    private void initView() {
        iv_head = findViewById(R.id.iv_gate);
        iv_face = findViewById(R.id.iv_face);
        tv_name = findViewById(R.id.tv_gage_name);
        tv_direction = findViewById(R.id.tv_direction);

        findViewById(R.id.btn_gosetting).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);

        findViewById(R.id.btn_import).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gosetting:
                //this.startActivity(new Intent(mContext, MainSettingActivity.class));
                break;
            case R.id.btn_exit:
                //finish();
                break;

            case R.id.btn_import:
                //通过指定的方式导入压缩的用户数据
                ImportFileManager.getInstance().batchImport();
                break;
            default:
        }
    }


    public void  push(Bitmap bitmap, User user, Bitmap cameraBitmap) {
//        mContext.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //iv_head.setImageBitmap(FaceSDKManager.getInstance().testCropFace(cameraBitmap));
//                iv_head.setImageBitmap(bitmap);
//                iv_head.setVisibility(View.VISIBLE);
//                tv_name.setText(user.name);
//            }
//        });

    }

    public void setFaceDataCallback(GateActivityReverse gateActivityReverse) {
    }
}