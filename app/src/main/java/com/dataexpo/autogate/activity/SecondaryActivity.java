package com.dataexpo.autogate.activity;

import android.app.Application;
import android.app.Presentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;

import java.util.Calendar;

public class SecondaryActivity extends Presentation implements EZUIPlayer.EZUIPlayerCallBack {
    private Application application = null;
    private Context context;
    private SurfaceView surface;
    private EZUIPlayer player;

    public SecondaryActivity(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);
        surface = findViewById(R.id.suface);
        initView();
        showCamera();
    }

    private void showCamera() {
        preparePlay();
        setSurfaceSize();
    }

    private void preparePlay() {
        //设置debug模式，输出log信息
        EZUIKit.setDebug(true);

        EZUIKit.initWithAppKey(application, "919af3ed2cce4c578488f90afb713cf8");

        //设置授权accesstoken
        EZUIKit.setAccessToken("at.a41nbev272svp9130wt96nuq0hljbvlh-6iicctpv1o-1xi9vt4-acfgezkvd");
        //设置播放资源参数
        player.setCallBack(this);
        player.setUrl("ezopen://open.ys7.com/D77714443/1.hd.live");
    }

    private void setSurfaceSize(){
//        DisplayMetrics dm = new DisplayMetrics();
//        application.getWindowManager().getDefaultDisplay().getMetrics(dm);
//        boolean isWideScrren = mOrientationDetector.isWideScrren();
//        //竖屏
//        if (!isWideScrren) {
//            //竖屏调整播放区域大小，宽全屏，高根据视频分辨率自适应
           //player.setSurfaceSize(500, 0);
//        } else {
//            //横屏屏调整播放区域大小，宽、高均全屏，播放区域根据视频分辨率自适应
            //player.setSurfaceSize(500, 500);
//        }
    }

    private void initView() {
        //获取视频播放窗口实例
        player = findViewById(R.id.camera_show);
        player.setLoadingView(initProgressBar());
    }

    public SurfaceView getSurfaceView() {
        return surface;
    }

    /**
     * 创建加载view
     * @return
     */
    private View initProgressBar() {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(lp);
        RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);//addRule参数对应RelativeLayout XML布局的属性
        ProgressBar mProgressBar = new ProgressBar(context);
        mProgressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress));
        relativeLayout.addView(mProgressBar,rlp);
        return relativeLayout;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //界面stop时，如果在播放，那isResumePlay标志位置为true，以便resume时恢复播放

        //停止播放
        player.stopPlay();
    }

    @Override
    public void show() {
        super.show();
        player.startPlay();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        player.releasePlayer();
    }

    @Override
    public void onPlaySuccess() {

    }

    @Override
    public void onPlayFail(EZUIError ezuiError) {
        if (ezuiError.getErrorString().equals(EZUIError.UE_ERROR_INNER_VERIFYCODE_ERROR)){

        }else if(ezuiError.getErrorString().equalsIgnoreCase(EZUIError.UE_ERROR_NOT_FOUND_RECORD_FILES)){
            // TODO: 2017/5/12
            //未发现录像文件
            Toast.makeText(context, "未找到录像",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onVideoSizeChange(int i, int i1) {

    }

    @Override
    public void onPrepared() {
        player.startPlay();
    }

    @Override
    public void onPlayTime(Calendar calendar) {

    }

    @Override
    public void onPlayFinish() {

    }

    public void setApplication(Application application) {
        this.application = application;
    }

}
