package com.dataexpo.autogate.face.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.dataexpo.autogate.face.callback.CameraDataCallback;

public class CameraPreviewManagerSingal extends BaseCameraManager  implements TextureView.SurfaceTextureListener  {

    @Override
    public void startPreview(Context context, AutoTexturePreviewView textureView, int width, int height, CameraDataCallback cameraDataCallback) {
        super.startPreview(context, textureView, width, height, cameraDataCallback);
        mTextureView.getTextureView().setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        //Log.e(TAG, "--surfaceTexture--SurfaceTextureAvailable");
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        textureWidth = i;
        textureHeight = i1;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
        //Log.e(TAG, "--surfaceTexture--TextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        //Log.e(TAG, "--surfaceTexture--destroyed");
        mSurfaceCreated = false;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // Log.e(TAG, "--surfaceTexture--Updated");
    }
}
