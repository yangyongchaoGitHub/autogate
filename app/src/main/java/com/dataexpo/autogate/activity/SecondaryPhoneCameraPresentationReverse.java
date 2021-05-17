package com.dataexpo.autogate.activity;

import android.app.Presentation;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.GateService;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
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
    private ImageView tv_bg;
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
    private Integer imgRrequestNum = 0;
    private Map<Integer, ReportData> requestMap = new HashMap<>();
    private Map<Integer, IQuery> imgRequestMap = new HashMap<>();

    private BgThread bgThread = null;

    private String currUrl = "";

    public SecondaryPhoneCameraPresentationReverse(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;
        mRetrofit = MainApplication.getmRetrofit();
    }

    class IQuery {
        public static final int SRequest = 1;
        public static final int SResponse = 2;

        Integer qid;
        int status;
        User user;
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
        if (bgThread == null) {
            bgThread = new BgThread();
            bgThread.start();
        }
    }

    private void initView() {
        iv_head = findViewById(R.id.iv_gate);
        iv_face = findViewById(R.id.iv_face);
        tv_name = findViewById(R.id.tv_gage_name);
        tv_direction = findViewById(R.id.tv_direction);
        tv_bg = findViewById(R.id.tv_bg);

        findViewById(R.id.btn_gosetting).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);

        findViewById(R.id.btn_import).setOnClickListener(this);

        iv_pass_status = findViewById(R.id.iv_pass_status);
        progress_view = findViewById(R.id.progress_view);

        String path = FileUtils.getBgDirectory().getPath() + "/bg.jpg";
        //final Bitmap bitmap = BitmapFactory.decodeFile(path);
        //Log.i(TAG, " responseData image path: " + path);

        tv_bg.setBackground(Drawable.createFromPath(path));
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
                        //iv_head.setImageResource(R.drawable.err);
                        tv_name.setText("");
                        GateService.getInstance().ledCtrl(LED_RED, mReports.getRfid());
                        return;
                    }

                    User res = UserService.getInstance().findUserByCardCode(user);

                    if (res != null) {
                        //有此用户
                        //String path = FileUtils.getUserPic(res.image_name);

                        //Log.i(TAG, " url:   " + res.image_base64);

                        String path = FileUtils.getUserPic(res.code);
                        final Bitmap bitmap = BitmapFactory.decodeFile(path);
                        currUrl = res.image_base64;
                        if ( bitmap == null && res.image_base64 != null && res.image_base64.startsWith("http")) {
                            //h获取人像
                            downloadImage(res);
                        } else {
                            iv_head.setImageBitmap(bitmap);
                            iv_head.setVisibility(View.VISIBLE);
                        }
                        //Log.i(TAG, " responseData image path: " + path);
                        
                        if (MainApplication.getInstance().getpModel() == 1 &&
                                MainApplication.getInstance().getPermissions() != null) {
                            //查询权限
                            queryPermission(mReports);
                            bQuery = true;
                        } else {
                            tv_name.setText(res.name);
                        }

                    } else {
                        GateService.getInstance().ledCtrl(LED_GREEN, mReports.getRfid());
                        iv_head.setVisibility(View.INVISIBLE);
                        tv_name.setText("嘉宾");
                    }
                    //tv_direction.setText("In".equals(mReports.getDirection()) ? "进" : "出");

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

    //当校验模式时，需要查询权限
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

    @Override
    protected void onStop() {
        super.onStop();
        bgThread.exist();
        bgThread = null;
    }

    class BgThread extends Thread {
        private boolean running = true;
        @Override
        public void run() {
            while (running) {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (MainApplication.getInstance().isbChange()) {
                    iv_head.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = FileUtils.getBgDirectory().getPath() + "/bg.jpg";
                            //final Bitmap bitmap = BitmapFactory.decodeFile(path);
                            //Log.i(TAG, " responseData image path: " + path);

                            tv_bg.setBackground(Drawable.createFromPath(path));
                        }
                    });
                    MainApplication.getInstance().setbChange(false);
                }
            }
        }

        public void exist() {
            running = false;
        }
    }


    private void downloadImage(User user) {
        IQuery query = new IQuery();
        query.qid = imgRrequestNum++;
        query.status = IQuery.SRequest;
        query.user = user;

        imgRequestMap.put(imgRrequestNum - 1, query);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(user.image_base64)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Iterator<Map.Entry<Integer, IQuery>> iterator = imgRequestMap.entrySet().iterator();

                while (iterator.hasNext()) {
                    IQuery query1 = iterator.next().getValue();
                    if (query1.user.image_base64.equals(call.request().url().toString())) {
                        iterator.remove();
                        break;
                    }
                }

                iv_head.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.i(TAG, "onFailure" + e.toString());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                ResponseBody result = response.body();

                Iterator<Map.Entry<Integer, IQuery>> iterator = imgRequestMap.entrySet().iterator();
                IQuery query = null;
                while (iterator.hasNext()) {
                    query = iterator.next().getValue();
                    if (query.user.image_base64.equals(call.request().url().toString())) {
                        iterator.remove();
                        break;
                    }
                }

                if (query == null) {
                    iv_head.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "请求过期 ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                if (result != null) {
                    // 获取图片
                    Bitmap bitmap = BitmapFactory.decodeStream(result.byteStream());
                    iv_head.post(new Runnable() {
                        @Override
                        public void run() {
                            if (currUrl.equals(call.request().url().toString())) {
                                iv_head.setImageBitmap(bitmap);
                                iv_head.setVisibility(View.VISIBLE);
                            }
                            //通知 小屏刷新
                            MainApplication.getInstance().setbQueryImgEnd(true);
                            //iv_sync_curr.setImageBitmap(bitmap);
                        }
                    });

                    fixImgLocal(bitmap, query.user);
                } else {
                    Log.i(TAG, "图像不存在 ");
                }
            }
        });
    }


    //将请求到的图像
    private void fixImgLocal(Bitmap bitmap, User user) {
        //把bitmap存到本地文件中
        File rootFile = FileUtils.getRegistedDirectory();
        File file = new File(rootFile.getPath() + "/" + user.code + ".jpg");

        if (FileUtils.saveBitmap(file, bitmap)) {
            UserService.getInstance().updateToSyncOk(user.pid, user.code);
        }
    }
}