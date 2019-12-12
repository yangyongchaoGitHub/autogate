package com.dataexpo.autogate.face.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.dataexpo.autogate.face.callback.CameraDataCallback;
import com.dataexpo.autogate.face.model.SingleBaseConfig;

import java.io.IOException;
import java.util.List;

public class BaseCameraManager {
    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_USB = 2;

    public static final int CAMERA_ORBBEC = 3;

    AutoTexturePreviewView mTextureView;
    SurfaceTexture mSurfaceTexture;

    int previewWidth;
    int previewHeight;

    int videoWidth;
    int videoHeight;

    int textureWidth;
    int textureHeight;

    Camera mCamera;
    CameraDataCallback mCameraDataCallback;
    private int cameraId = 0;

    /**
     * 当前相机的ID。
     */
    int cameraFacing = CAMERA_FACING_FRONT;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    int displayOrientation = 0;
    boolean mSurfaceCreated = false;

    int getCameraDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;

        //Log.i(TAG, "getCameraDisplayOrientation " + info.facing + " orientation " + info.orientation + " " + degrees);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    /**
     * 开启摄像头
     */
    void openCamera() {
        try {
            if (mCamera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraFacing) {
                        cameraId = i;
                    }
                }
                mCamera = Camera.open(cameraId);
                //Log.e(TAG, "initCamera---open camera");
            }

            // 摄像头图像预览角度
            int cameraRotation = SingleBaseConfig.getBaseConfig().getVideoDirection();
            switch (cameraFacing) {
                case CAMERA_FACING_FRONT: {
                    cameraRotation = ORIENTATIONS.get(displayOrientation);
                    cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId);
                    mCamera.setDisplayOrientation(cameraRotation);
                }
                break;
                case CAMERA_FACING_BACK: {
                    cameraRotation = ORIENTATIONS.get(displayOrientation);
                    cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId);
                    mCamera.setDisplayOrientation(cameraRotation);
                }
                break;
                case CAMERA_USB: {
                    mCamera.setDisplayOrientation(cameraRotation);
                }
                break;
                default:
                    break;

            }

            if (SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                mTextureView.setRotationY(180);
            } else {
                mTextureView.setRotationY(0);
            }

            if (cameraRotation == 90 || cameraRotation == 270) {
                // 旋转90度或者270，需要调整宽高
                Log.i("CameraPreviewManager", "90 or 270 setPreviewSize " + previewHeight + " " + previewWidth);
                mTextureView.setPreviewSize(previewHeight, previewWidth);

            } else {
                Log.i("CameraPreviewManager", " setPreviewSize " + previewHeight + " " + previewWidth);
                mTextureView.setPreviewSize(previewWidth, previewHeight);
            }

            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, previewWidth,
                    previewHeight); // 获取一个最为适配的camera.size

            videoWidth = optionSize.width;
            videoHeight = optionSize.height;

            params.setPreviewSize(videoWidth, videoHeight);
            mCamera.setParameters(params);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();

                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(bytes, camera,
                                    videoWidth, videoHeight);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                //Log.e(TAG, e.getMessage());
            }
        } catch (RuntimeException e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 解决预览变形问题
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        //Log.i(TAG, "setOptimalPreviewSize w:" + w + " h:" + h);

        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            //Log.i(TAG, "Camera size.width:" + size.width + " height: " + size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        //Log.i(TAG, "end  --:" + optimalSize.width + " height: " + optimalSize.height);
        return optimalSize;
    }

    /**
     * 暂停预览
     */
    public void waitPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void stopWaitPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    if (mCameraDataCallback != null) {
                        mCameraDataCallback.onGetCameraData(bytes, camera,
                                videoWidth, videoHeight);
                    }
                }
            });
        }

    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(null);
                mSurfaceCreated = false;
                mTextureView = null;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e("qing", "camera destory error");
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启预览
     *
     * @param context
     * @param textureView
     */
    public void startPreview(Context context, AutoTexturePreviewView textureView, int width,
                             int height, CameraDataCallback cameraDataCallback) {
        //Log.e(TAG, "开启预览模式");
        Context mContext = context;
        this.mCameraDataCallback = cameraDataCallback;
        mTextureView = textureView;
        this.previewWidth = width;
        this.previewHeight = height;
        mSurfaceTexture = mTextureView.getTextureView().getSurfaceTexture();
        //mTextureView.getTextureView().setSurfaceTextureListener(this);
    }
}