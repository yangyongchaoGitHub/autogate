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
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.face.camera.AutoTexturePreviewView;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.model.service.Device;
import com.dataexpo.autogate.model.service.MsgBean;
import com.dataexpo.autogate.model.service.Permissions;
import com.dataexpo.autogate.model.service.RegStatus;
import com.dataexpo.autogate.model.service.UserAndPermission;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.GateService;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;

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

    private ImageView iv_pass_status;
    private CircularProgressView progress_view;

    private ServiceConnection mConnection;
    private AutoTexturePreviewView mAutoCameraPreviewView;

    private static final int PREFER_WIDTH = 1280;
    private static final int PERFER_HEIGH = 720;

    private Retrofit mRetrofit;
    private Integer requestNum = 0;
    private Map<Integer, ReportData> requestMap = new HashMap<>();

    public SecondaryPhoneCameraPresentationReverse(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;
        mRetrofit = MainApplication.getmRetrofit();
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

        iv_pass_status = findViewById(R.id.iv_pass_status);
        progress_view = findViewById(R.id.progress_view);
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
                    boolean bQuery = false;

                    iv_pass_status.setVisibility(View.INVISIBLE);
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

                        if (MainApplication.getInstance().getpModel() == 1 &&
                                MainApplication.getInstance().getPermissions() != null) {
                            //查询权限
                            queryPermission(mReports);
                            bQuery = true;
                        } else {
                            tv_name.setText(res.name);
                        }

                    } else {
                        iv_head.setVisibility(View.INVISIBLE);
                        tv_name.setText("未注册！");
                    }
                    tv_direction.setText("In".equals(mReports.getDirection()) ? "进" : "出");

                    //没有向服务器查询，则表示不符合
                    if (MainApplication.getInstance().getpModel() == 1 &&
                            MainApplication.getInstance().getPermissions() != null && !bQuery) {
                        mReports.setTime(Utils.timeNowLong());
                        mReports.setModel(1);
                        mReports.setAddress(MainApplication.getInstance().getPermissions().getNames());
                        mReports.setStatus(1);
                        mReports.setPermissionid(MainApplication.getInstance().getPermissions().getId());
                        CardService.getInstance().insert(mReports);
                    }
                }
            }
        });
    }

    @Override
    public void responseStatus(Rfid rfid) {

    }

    private void queryPermission(ReportData mReports) {
        iv_head.post(new Runnable() {
            @Override
            public void run() {
                progress_view.setVisibility(View.VISIBLE);
            }
        });

        ApiService apiService = mRetrofit.create(ApiService.class);

        Call<MsgBean> call = apiService.checkCard(mReports.getNumber());
        requestMap.put(call.hashCode(), mReports);

        call.enqueue(new Callback<MsgBean>() {
            @Override
            public void onResponse(Call<MsgBean> call, Response<MsgBean> response) {
                MsgBean result = response.body();
                iv_head.post(new Runnable() {
                    @Override
                    public void run() {
                        progress_view.setVisibility(View.INVISIBLE);
                        ReportData reportData = requestMap.get(call.hashCode());

                        if (reportData == null) {
                            Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (result == null) {
                            Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Permissions permissions = MainApplication.getInstance().getPermissions();

                        if (result.data != null) {
                            UserAndPermission userAndPermission = result.data;
                            Log.i(TAG, " result.data  " + result.data.toString());

                            //获取这个观众的权限
                            List<RegStatus> regStatuses = userAndPermission.getRegList();
                            int status = 1;
                            for (RegStatus r: regStatuses) {
                                if (permissions.getId() == r.getRegionId()) {
                                    status = 2;
                                    break;
                                }
                            }

                            Log.i(TAG, "check  status   " + status);

                            //有权限
                            if (status == 2) {
                                tv_name.setText(userAndPermission.getUiName());
                                iv_pass_status.setImageResource(R.drawable.success);
                                //发送控制信号
                                GateService.getInstance().ledCtrl(LED_GREEN, reportData.getRfid());
                            } else {
                                iv_pass_status.setImageResource(R.drawable.error);
                                //发送控制信号
                                GateService.getInstance().ledCtrl(LED_RED, reportData.getRfid());
                            }

                            iv_pass_status.setVisibility(View.VISIBLE);

                            //保存日志
                            mReports.setTime(Utils.timeNowLong());
                            mReports.setModel(1);
                            mReports.setPid(userAndPermission.getEuId());
                            mReports.setAddress(MainApplication.getInstance().getPermissions().getNames());
                            mReports.setPermissionid(MainApplication.getInstance().getPermissions().getId());
                            mReports.setStatus(status);
                            CardService.getInstance().insert(mReports);

                        } else {
                            mReports.setTime(Utils.timeNowLong());
                            mReports.setModel(1);
                            mReports.setAddress(MainApplication.getInstance().getPermissions().getNames());
                            mReports.setStatus(1);
                            mReports.setPermissionid(MainApplication.getInstance().getPermissions().getId());
                            CardService.getInstance().insert(mReports);
                            iv_head.setVisibility(View.INVISIBLE);
                            iv_head.setImageResource(R.drawable.err);
                            tv_name.setText("非法通过！");
                            return;
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<MsgBean> call, Throwable t) {
                iv_head.post(new Runnable() {
                    @Override
                    public void run() {
                        ReportData reportData = requestMap.get(call.hashCode());

                        if (reportData != null) {
                            mReports.setTime(Utils.timeNowLong());
                            mReports.setModel(1);
                            mReports.setAddress(MainApplication.getInstance().getPermissions().getNames());
                            mReports.setStatus(1);
                            mReports.setPermissionid(MainApplication.getInstance().getPermissions().getId());
                            CardService.getInstance().insert(mReports);
                        }

                        progress_view.setVisibility(View.INVISIBLE);
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.i(TAG, "onFailure" + t.toString());
            }
        });
    }
}