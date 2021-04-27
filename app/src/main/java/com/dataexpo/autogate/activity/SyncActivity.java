package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.model.service.mp.MsgBean;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.comm.Utils.EXPO_ID;

public class SyncActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = SyncActivity.class.getSimpleName();

    private Button btn_sync_user;
    private Button btn_sync_record;
    //同步用户数据接口
    private Button btn_check;

    private ConstraintLayout cl_select;
    private ConstraintLayout cl_user;
    private ConstraintLayout cl_record;

    private CircularProgressView progress_view;
    private CircularProgressView progress_view_mini; //同步基础数据内部的进度
    private CircularProgressView progress_view_img; //同步人像进度

    private TextView tv_server_count_value;
    private TextView tv_local_count_value;
    private TextView tv_curr_count;
    private TextView tv_img_local_count_value;

    private TextView tv_img_total_value;

    private ImageView iv_sync_curr;

    private ImageView iv_success_base;
    private ImageView iv_success_img;

    private Retrofit mRetrofit;

    //同步用户数据时的参数
    private int totalServiceSize = 0;
    private int totalLocalSize = 0;
    private int currChange = 0;
    private int minPid = 0;

    //同步图像数据
    private int totalImgSize = 0;
    private int currImgChange = 0;

    private int requestNo = 0;

    //同步线程
    UserSyncThread ust = null;

    UserImgSyncThread uist = null;

    //请求参数
    List<UserQueryConditionVo> requestList = new ArrayList<>();

    private int seed = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        mContext = this;
        mRetrofit = MainApplication.getmRetrofit();
        initView();
    }

    private void initView() {
        btn_sync_user = findViewById(R.id.btn_sync_user);
        btn_sync_record = findViewById(R.id.btn_sync_record);

        btn_sync_user.setOnClickListener(this);
        btn_sync_record.setOnClickListener(this);

        cl_select = findViewById(R.id.cl_select);
        cl_user = findViewById(R.id.cl_user);
        cl_record = findViewById(R.id.cl_record);

        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(this);

        progress_view = findViewById(R.id.progress_view);
        progress_view_mini = findViewById(R.id.progress_view_mini);
        iv_success_base = findViewById(R.id.iv_success_base);
        iv_success_img = findViewById(R.id.iv_success_img);
        progress_view_img = findViewById(R.id.progress_view_img);
        tv_img_total_value = findViewById(R.id.tv_img_total_value);
        tv_img_local_count_value = findViewById(R.id.tv_img_local_count_value);
        //progress_view.setProgress(50);

        tv_server_count_value = findViewById(R.id.tv_server_count_value);
        tv_local_count_value = findViewById(R.id.tv_local_count_value);
        tv_curr_count = findViewById(R.id.tv_curr_count);

        iv_sync_curr = findViewById(R.id.iv_sync_curr);

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
        findViewById(R.id.tv_background).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_background:
                startActivity(new Intent(mContext, BackgroundActivity.class));
                break;

            case R.id.tv_back:
            case R.id.ib_back:
                this.finish();
                break;

            case R.id.btn_check:
                if ("".equals(Utils.getEXPOConfig(mContext, EXPO_ID))) {
                    Toast.makeText(mContext, "请先注册设备到服务器 ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (btn_check.getText().toString().equals("取消")) {
                    btn_check.setText("检查数据完整性");
                    progress_view_mini.setVisibility(View.INVISIBLE);
                    if (ust != null) {
                        ust.setRunningStop();
                    }
                    return;
                }
                if (btn_check.getText().toString().equals("取消同步人像")) {
                    btn_check.setText("检查数据完整性");
                    if (uist != null) {
                        uist.setRunningStop();
                    }
                    return;
                }

                if (ust != null && ust.running) {
                    Toast.makeText(mContext, "正在同步", Toast.LENGTH_SHORT).show();
                } else {

                    iv_success_base.setVisibility(View.INVISIBLE);
                    progress_view_mini.setVisibility(View.VISIBLE);
                    iv_success_img.setVisibility(View.INVISIBLE);
                    tv_img_total_value.setText("");
                    tv_img_local_count_value.setText("");
                    tv_curr_count.setText("");
                    ust = new UserSyncThread();
                    minPid = 0;
                    ust.start();
                    btn_check.setText("取消");
                }

                break;

            case R.id.btn_sync_user:
                cl_select.setVisibility(View.INVISIBLE);
                cl_user.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_sync_record:
                cl_select.setVisibility(View.INVISIBLE);
                cl_record.setVisibility(View.VISIBLE);
                break;

            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ust != null) {
            ust.setRunningStop();
        }
        if (uist != null) {
            uist.setRunningStop();
        }
    }

    //获取用户数据
    private UserQueryConditionVo queryUser(UserQueryConditionVo vo) {
        Log.i(TAG, "queryUser " + vo.getPageNo() + " pagenum: " + vo.getPageNo() + " size:" + vo.getPageSize());
        ApiService apiService = mRetrofit.create(ApiService.class);

        vo.setRequestStatus(UserQueryConditionVo.STATUS_REQUESTING);

        Call<MsgBean<PageResult<UserEntityVo>>> call = apiService.mpQuerySyncUser(vo);

        call.enqueue(new Callback<MsgBean<PageResult<UserEntityVo>>>() {
            @Override
            public void onResponse(Call<MsgBean<PageResult<UserEntityVo>>> call, Response<MsgBean<PageResult<UserEntityVo>>> response) {
                //Log.i(TAG, "onResponse" + response);

                MsgBean<PageResult<UserEntityVo>> result = response.body();
                if (result == null) {
                    for (UserQueryConditionVo u: requestList) {
                        if (u.getRequestId() == requestNo) {
                            try {
                                Thread.sleep(1500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                            break;
                        }
                    }
                    return;
                }

                fixLocal(result.data);
            }

            @Override
            public void onFailure(Call<MsgBean<PageResult<UserEntityVo>>> call, Throwable t) {
                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                        break;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
        requestList.add(vo);
        return vo;
    }

    private void downloadImage(UserQueryConditionVo vo) {
        vo.setRequestStatus(UserQueryConditionVo.STATUS_REQUESTING);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(vo.getImageBase64())
                .build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception e1) {

                        }
                        u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                        break;
                    }
                }

                runOnUiThread(new Runnable() {
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

                UserQueryConditionVo queryConditionVo = null;

                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        queryConditionVo = u;
                        break;
                    }
                }

                if (queryConditionVo == null) {
                    queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                    Toast.makeText(mContext, "请求过期", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result != null) {
                    // 获取图片
                    Bitmap bitmap = BitmapFactory.decodeStream(result.byteStream());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_sync_curr.setImageBitmap(bitmap);
                        }
                    });

                    fixImgLocal(bitmap, queryConditionVo);
                } else {
                    queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
                    Log.i(TAG, "图像不存在 " + queryConditionVo.getEucode() + " euid: " +queryConditionVo.getStartEuId());
                    //取消待同步状态
                    int res = UserService.getInstance().updateToNoSyncImg(queryConditionVo.getStartEuId());
                }
            }
        });
    }

    //获取用户数据
    private UserQueryConditionVo queryUserImage(UserQueryConditionVo vo) {
        Log.i(TAG, "queryUserImage " + vo.getPageNo());
        ApiService apiService = mRetrofit.create(ApiService.class);

        vo.setRequestStatus(UserQueryConditionVo.STATUS_REQUESTING);

        Call<ResponseBody> call = apiService.querySyncUserImage(vo.getEucode() + ".jpg");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.i(TAG, "onResponse" + response);

                ResponseBody result = response.body();

                UserQueryConditionVo queryConditionVo = null;

                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        queryConditionVo = u;
                        break;
                    }
                }

                if (queryConditionVo == null) {
                    queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                    Toast.makeText(mContext, "请求过期", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result != null) {
                    // 获取图片
                    Bitmap bitmap = BitmapFactory.decodeStream(result.byteStream());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_sync_curr.setImageBitmap(bitmap);
                        }
                    });

                    fixImgLocal(bitmap, queryConditionVo);
                } else {
                    queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
                    Log.i(TAG, "图像不存在 " + queryConditionVo.getEucode() + " euid: " +queryConditionVo.getStartEuId());
                    //取消待同步状态
                    int res = UserService.getInstance().updateToNoSyncImg(queryConditionVo.getStartEuId());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        try {
                            Thread.sleep(1500);
                        } catch (Exception e) {

                        }
                        u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                        break;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
        return vo;
    }

    //将请求到的图像
    private void fixImgLocal(Bitmap bitmap, UserQueryConditionVo queryConditionVo) {
        //把bitmap存到本地文件中
        File rootFile = FileUtils.getRegistedDirectory();
        File file = new File(rootFile.getPath() + "/" + queryConditionVo.getEucode() + ".jpg");

        if (FileUtils.saveBitmap(file, bitmap)) {
            UserService.getInstance().updateToSyncOk(queryConditionVo.getStartEuId(), queryConditionVo.getEucode());
            queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
        }
        fixProgressImgShow();
    }

    //将请求到的数据和本地的数据进行比较
    private void fixLocal(PageResult<UserEntityVo> objectList) {
        List<UserEntityVo> userEntityVos = objectList.getObjectList();

        totalServiceSize = objectList.getCount();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_server_count_value.setText(totalServiceSize + "");
            }
        });

        if (totalServiceSize > 0) {
            String minId = "", maxId = "";
            if (userEntityVos.size() >= 1) {
                if (minPid < userEntityVos.get(0).getEuId()) {
                    //Log.i(TAG, " minPid 1 " + minPid);
                    minId = minPid + "";
                } else {
                    //Log.i(TAG, " minPid 2 " + userEntityVos.get(0).getEuId());
                    minId = userEntityVos.get(0).getEuId() + "";
                }
                //Log.i(TAG, " maxId 1 " + userEntityVos.get(userEntityVos.size() - 1).getEuId());
                maxId = userEntityVos.get(userEntityVos.size() - 1).getEuId() + "";
                if (userEntityVos.size() < seed) {
                    //这时应该直接取后面的全部
                    maxId = (userEntityVos.get(userEntityVos.size() - 1).getEuId() + 10000000) + "";
                } else {
                    minPid = userEntityVos.get(userEntityVos.size() - 1).getEuId() + 1;
                }
            }

            // 根据最上，最下两个euid进行数据库查询，
            //一个list
            List<User> users = UserService.getInstance().findUserByEuId(minId, maxId);

            List<UserEntityVo> sUpdateList = new ArrayList<>();
            List<User> updateList = new ArrayList<>();

            boolean bExist;
            //从数据里面对比，不管多少还是相等，有没有多的，有没有少的
            //多的list进行新增
            Iterator<UserEntityVo> iue = userEntityVos.iterator();
            Iterator<User> iu;

            Log.i(TAG, "userEntityVos size " + userEntityVos.size() + " users size : " + users.size());

            while (iue.hasNext()) {
                UserEntityVo ue = iue.next();

                bExist = false;
                iu = users.iterator();
                //Log.i(TAG, "name: " + ue.getName() + " " + ue.getEuId());
                while (iu.hasNext()) {
                    User u = iu.next();
                    //Log.i(TAG, "name: " + ue.getName() + " " + ue.getEuId() + " " + u.pid + " " + u.position);
                    if (u.pid == ue.getEuId()) {
                        //原有已同步数据
                        //检查是否有更改
                        if (!UserService.getInstance().compareUser(u, ue)) {
                            updateList.add(u);
                            //有更改
                        }
                        //已经校验有修改或者没有修改，都将他们去除
                        iue.remove();
                        iu.remove();
                        fixProgressShow();

                        bExist = true;
                        break;
                    }
                }
                if (!bExist) {
                    sUpdateList.add(ue);
                    iue.remove();
                    fixProgressShow();
                }
            }

            //新增
            if (sUpdateList.size() > 0) {
                Log.i(TAG, "sUpdateList.size() " + sUpdateList.size());
                UserService.getInstance().addDataFromService(sUpdateList, Integer.parseInt(Utils.getEXPOConfig(mContext, EXPO_ID)));
            }

            //修改
            if (updateList.size() > 0) {
                Log.i(TAG, "updateList.size() " + updateList.size());
                UserService.getInstance().updateFromService(updateList);
            }

            //删除
            if (users.size() > 0) {
                Log.i(TAG, "users.size() " + users.size());
                UserService.getInstance().removeServiceNoExist(users);
            }
        } else {
            UserService.getInstance().removeAll();
        }

        //查询时的查询条件
        for (UserQueryConditionVo u: requestList) {
            if (u.getRequestId() == requestNo) {
                u.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
                break;
            }
        }
    }

    private void fixProgressShow() {
        currChange++;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_view_mini.setVisibility(View.INVISIBLE);
                tv_curr_count.setText("已同步: " + currChange + "");
                progress_view.setProgress((float) ((double) currChange / (double) totalServiceSize) * 100);
            }
        });
    }

    private void fixProgressImgShow() {
        currImgChange++;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_img_local_count_value.setText(currImgChange + "");
                progress_view_img.setProgress((float) ((double) currImgChange / (double) totalImgSize) * 100);
            }
        });
    }

    //用户数据同步线程
    class UserSyncThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            totalServiceSize = 0;
            currChange = 0;
            totalLocalSize = UserService.getInstance().countLocal();

            int pageNo = 1;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_local_count_value.setText(totalLocalSize + "");
                }
            });

            UserQueryConditionVo conditionVo = null;

            //同步之前先删除不是本次同步的数据 根据expoId
            UserService.getInstance().removeAllByExpoId(Integer.parseInt(Utils.getEXPOConfig(mContext, EXPO_ID)));

            while (running) {
                //按批次进行服务器用户数据进行同步
                if (conditionVo == null) {
                    conditionVo = new UserQueryConditionVo();
                    conditionVo.setPageNo(pageNo);
                    conditionVo.setPageSize(seed);
                    conditionVo.setRequestId(++requestNo);
                    conditionVo.setThreadName(this.getName());
                    conditionVo.setExpoId(Integer.parseInt(Utils.getEXPOConfig(mContext, EXPO_ID)));
                    conditionVo = queryUser(conditionVo);

                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_RESPONSE) {
                    //已经取完
                    Log.i(TAG, "currChange: " + currChange + " totalServiceSize: " + totalServiceSize);

                    //if (currChange == totalServiceSize) {
                    if ((pageNo) * seed > totalServiceSize) {
                        setRunningStop();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_check.setText("取消同步人像");
                                Toast.makeText(mContext, "基础数据同步结束", Toast.LENGTH_SHORT).show();
                                totalLocalSize = UserService.getInstance().countLocal();
                                tv_local_count_value.setText(totalLocalSize + "");

                                progress_view.setProgress(0);
                                iv_success_base.setVisibility(View.VISIBLE);

                                int syncCount = UserService.getInstance().countImageLocalNoSync();
                                if (syncCount > 0) {
                                    Log.i(TAG, "syncCount > 0   goto sync img ");
                                    if (uist == null) {
                                        uist = new UserImgSyncThread();
                                        uist.start();
                                    }
                                } else {
                                    progress_view_mini.setVisibility(View.INVISIBLE);
                                    tv_img_total_value.setText("0");
                                    tv_curr_count.setText("");
                                    Log.i(TAG, "syncCount !> 0   sync end ");
                                    btn_check.setText("检查数据完整性");
                                    Toast.makeText(mContext, "同步结束", Toast.LENGTH_SHORT).show();
                                    setRunningStop();
                                }
                            }
                        });
                        return;
                    }

                    requestList.remove(conditionVo);
                    conditionVo = new UserQueryConditionVo();
                    conditionVo.setPageNo(++pageNo);
                    conditionVo.setPageSize(seed);
                    conditionVo.setRequestId(++requestNo);
                    conditionVo.setThreadName(this.getName());
                    conditionVo.setExpoId(Integer.parseInt(Utils.getEXPOConfig(mContext, EXPO_ID)));
                    conditionVo = queryUser(conditionVo);

                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_FAIL) {
                    conditionVo = queryUser(conditionVo);
                }

                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunningStop() {
            running = false;
        }
    }

    //用户图像数据同步线程
    class UserImgSyncThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            currImgChange = 0;
            totalImgSize = UserService.getInstance().countImageLocalNoSync();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //progress_view.setProgress(0);
                    progress_view_img.setProgress(0);
                    tv_img_total_value.setText(totalImgSize + "");
                }
            });

            UserQueryConditionVo conditionVo = null;
            while (running) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (conditionVo == null || conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_RESPONSE) {
                    //一条一条进行查询
                    User user = UserService.getInstance().findNoSyncImgOne();

                    if (user != null) {
                        Log.i(TAG, "user != null " + user.code );
                        conditionVo = new UserQueryConditionVo();
                        conditionVo.setEucode(user.code);
                        conditionVo.setStartEuId(user.pid);
                        conditionVo.setRequestId(++requestNo);
                        conditionVo.setThreadName(this.getName());
                        conditionVo.setImageBase64(user.image_base64);
                        downloadImage(conditionVo);
                        requestList.add(conditionVo);

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress_view_img.setProgress(0);
                                iv_success_img.setVisibility(View.VISIBLE);
                                iv_sync_curr.setImageDrawable(null);
                                iv_sync_curr.setVisibility(View.INVISIBLE);
                                btn_check.setText("检查数据完整性");
                                Toast.makeText(mContext, "同步结束", Toast.LENGTH_SHORT).show();
                                setRunningStop();
                                uist = null;
                            }
                        });
                    }
                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_FAIL) {
                    downloadImage(conditionVo);
                }
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunningStop() {
            running = false;
        }
    }
}
