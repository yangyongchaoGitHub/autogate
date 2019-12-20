package com.dataexpo.autogate.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import com.dataexpo.autogate.comm.BitmapUtils;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.face.model.LivenessModel;
import com.dataexpo.autogate.model.FaceRecord;
import com.dataexpo.autogate.model.User;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FaceService {
    private static final String TAG = FaceService.class.getSimpleName();

    private FaceService() {};

    private static class HolderClass {
        private static final FaceService instance = new FaceService();
    }

    /**
     * 单例模式
     */
    public static FaceService getInstance() {
        return FaceService.HolderClass.instance;
    }

    public int saveFaceRecord(LivenessModel livenessModel) {
        //1 保存图片
        //2 保存其他记录到数据库
        User u = livenessModel.getUser();

        FaceRecord record = new FaceRecord();
        record.user_id = u.id;
        record.user_code = u.code;
        record.time = Utils.timeNowLong();
        record.company = u.company;
        record.cardcode = u.cardCode;
        record.name = u.name;
        record.imageName = record.user_id + record.time;

        Bitmap bitmap = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());

        Log.i(TAG, "saveFaceRecord pic path: " + record.imageName);
        //save image
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(FileUtils.getFacePicPath(record.imageName));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        insert(record);
        return 0;
    }

    public List<FaceRecord> listAll() {
        ArrayList<FaceRecord> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_FACE_RECORD, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            FaceRecord response = resolve(cursor);
            if (response != null) {
                list.add(response);
            }
        }

        cursor.close();
        return list;
    }

    private long insert(FaceRecord record) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", record.user_id);
        contentValues.put("code", record.user_code);
        contentValues.put("time", record.time);
        contentValues.put("imagename", record.imageName);
        contentValues.put("company", record.company);
        contentValues.put("cardcode", record.cardcode);
        contentValues.put("name", record.name);
        return DBUtils.getInstance().insert(DBUtils.TABLE_FACE_RECORD, null, contentValues);
    }

    private FaceRecord resolve(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        FaceRecord response = new FaceRecord();
        response.id = cursor.getInt(cursor.getColumnIndex("id"));
        response.user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
        response.user_code = cursor.getString(cursor.getColumnIndex("code"));
        response.time = cursor.getString(cursor.getColumnIndex("time"));
        response.company = cursor.getString(cursor.getColumnIndex("company"));
        response.cardcode = cursor.getString(cursor.getColumnIndex("cardcode"));
        response.imageName = cursor.getString(cursor.getColumnIndex("imagename"));
        response.name = cursor.getString(cursor.getColumnIndex("name"));
        return response;
    }
}
