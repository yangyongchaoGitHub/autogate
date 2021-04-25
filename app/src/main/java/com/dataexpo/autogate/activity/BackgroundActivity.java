package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 背景图
 */
public class BackgroundActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = BackgroundActivity.class.getSimpleName();

    private Retrofit mRetrofit;

    private ImageView iv_curr_background;

    private CircularProgressView progress_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        mContext = this;
        mRetrofit = MainApplication.getmRetrofit();
        initView();
    }

    private void initView() {
        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);

        iv_curr_background = findViewById(R.id.iv_curr_background);
        progress_view = findViewById(R.id.progress_view);
        progress_view.setVisibility(View.INVISIBLE);

        findViewById(R.id.btn_check).setOnClickListener(this);

        String path = FileUtils.getBgDirectory().getPath() + "/bg.jpg";
        //final Bitmap bitmap = BitmapFactory.decodeFile(path);
        Log.i(TAG, " responseData image path: " + path);

        iv_curr_background.setBackground(Drawable.createFromPath(path));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();
                break;

            case R.id.btn_check:
                progress_view.setVisibility(View.VISIBLE);
                queryBg();
                break;

            default:
        }
    }

    //获取用户数据
    private void queryBg() {
        ApiService apiService = mRetrofit.create(ApiService.class);

        Call<ResponseBody> call = apiService.queryBackground();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "onResponse" + response);

                ResponseBody result = response.body();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result != null) {
                            // 获取图片
                            Bitmap bitmap = BitmapFactory.decodeStream(result.byteStream());


                            File rootFile = FileUtils.getBgDirectory();
                            File file = new File(rootFile.getPath() + "/bg.jpg");

                            if (FileUtils.saveBitmap(file, bitmap)) {
                                //通知更改背景图
                                MainApplication.getInstance().setbChange(true);

                                //更新当前显示
                                String path = FileUtils.getBgDirectory().getPath() + "/bg.jpg";
                                //final Bitmap bitmap = BitmapFactory.decodeFile(path);
                                Log.i(TAG, " responseData image path: " + path);

                                iv_curr_background.setBackground(Drawable.createFromPath(path));
                            }

                        } else {
                            Toast.makeText(mContext, "图像不存在", Toast.LENGTH_SHORT).show();
                            //Log.i(TAG, "图像不存在 ");
                        }
                        progress_view.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                        progress_view.setVisibility(View.INVISIBLE);
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
    }

}
