package com.dataexpo.autogate.face.service;


public class FaceUserService {
    private static final String TAG = FaceUserService.class.getSimpleName();
    private FaceUserService() {};

    private static class HolderClass {
        private static final FaceUserService instance = new FaceUserService();
    }

    /**
     * 单例模式
     */
    public static FaceUserService getInstance() {
        return FaceUserService.HolderClass.instance;
    }
}
