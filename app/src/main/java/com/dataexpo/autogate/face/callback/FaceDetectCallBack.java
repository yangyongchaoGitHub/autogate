/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.dataexpo.autogate.face.callback;


import com.dataexpo.autogate.face.model.LivenessModel;

/**
 * 人脸检测回调接口。
 *
 */
public interface FaceDetectCallBack {
    public void onFaceDetectCallback(LivenessModel livenessModel);

    public void onTip(int code, String msg);

    void onFaceDetectDarwCallback(LivenessModel livenessModel);
}
