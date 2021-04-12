package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;

/**
 * 原分屏是小屏，现分屏是大屏，所以做次调整
 */
public class SecondaryPhoneCameraPresentationReverse extends Presentation implements View.OnClickListener, GateObserver {
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

        //注册消费者
        MainApplication.getInstance().getService().removeGateObserver(SecondaryPhoneCameraPresentationReverse.this);
        MainApplication.getInstance().getService().addGateObserver(SecondaryPhoneCameraPresentationReverse.this);
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
                //ImportFileManager.getInstance().batchImport();
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

    public void setFaceDataCallback(MainSelectActivity mainSelectActivity) {
    }


    @Override
    public void responseData(final ReportData mReports) {
        iv_head.post(new Runnable() {
            @Override
            public void run() {
                if (mReports != null) {
                    Log.i(TAG, "responseData card: " + mReports.getNumber() + " time " + mReports.getTime());
                    User user = new User();
                    user.cardCode = mReports.getNumber();

                    if ("FFFFFFFFFFFFFFFF".equals(mReports.getNumber())) {
                        iv_head.setVisibility(View.INVISIBLE);
                        iv_head.setImageResource(R.drawable.err);
                        tv_name.setText("非法通过！");
                        return;
                    }

                    User res = UserService.getInstance().findUserByCardCode(user);

                    if (res != null) {
                        //有此用户
                        String path = FileUtils.getUserPic(res.image_name);
                        final Bitmap bitmap = BitmapFactory.decodeFile(path);
                        Log.i(TAG, " responseData image path: " + path);

                        iv_head.setImageBitmap(bitmap);
                        iv_head.setVisibility(View.VISIBLE);

                        tv_name.setText(res.name);

                    } else {
                        iv_head.setVisibility(View.INVISIBLE);
                        tv_name.setText("未注册！");
                    }
                    tv_direction.setText("In".equals(mReports.getDirection()) ? "进" : "出");
                }
            }
        });
    }

    @Override
    public void responseStatus(Rfid rfid) {

    }
}