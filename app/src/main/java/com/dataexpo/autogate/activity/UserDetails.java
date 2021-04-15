package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.face.api.FaceApi;
import com.dataexpo.autogate.model.User;

import static com.dataexpo.autogate.face.manager.ImportFileManager.IMPORT_SUCCESS;

public class UserDetails extends BascActivity implements View.OnClickListener {
    private static final String TAG = UserDetails.class.getSimpleName();
    private Button    btn_back;
    private ImageView iv_image;
    private TextView  tv_name;
    private TextView  tv_company;
    private TextView  tv_deputation;
    private TextView  faceregistr;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        user = (User) getIntent().getSerializableExtra(Utils.EXTRA_EXPO_USRE);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_user_details_back).setOnClickListener(this);
        iv_image = findViewById(R.id.iv_user_info_image);
        tv_name = findViewById(R.id.tv_user_info_name_value);
        tv_company = findViewById(R.id.tv_user_info_company_value);
        tv_deputation = findViewById(R.id.tv_user_info_deputation_value);
        faceregistr = findViewById(R.id.tv_user_info_register_value);
        faceregistr.setOnClickListener(this);
        initInfo(user);
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    private void initInfo(User user) {
        tv_name.setText(user.name);
        tv_company.setText(user.company);
        tv_deputation.setText(user.position);
        faceregistr.setText(user.bregist_face == IMPORT_SUCCESS  ? "已注册" : "未注册");
        Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
        iv_image.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_user_details_back:
            case R.id.ib_back:
                finish();
                break;

            case R.id.tv_user_info_register_value:
                if (user.bregist_face != IMPORT_SUCCESS && !"".equals(user.image_name)) {
                    //未注册人脸且用户图片存在则在点击时进行人脸注册
                    int result = FaceApi.getInstance().registFaceByImage(user,
                            FileUtils.getUserPic(user.image_name));
                    Log.i(TAG, " registFace result: " + result);
                    if (result == IMPORT_SUCCESS) {
                        user.bregist_face = IMPORT_SUCCESS;
                        try {
                            FaceApi.getInstance().initDatabases(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

                default:
        }
    }
}
