package com.dataexpo.autogate.listener;

import android.graphics.Bitmap;

import com.dataexpo.autogate.model.User;

public interface FaceOnSecCallback {
    void push(Bitmap bitmap, User user, Bitmap cameraBitmap);
}
