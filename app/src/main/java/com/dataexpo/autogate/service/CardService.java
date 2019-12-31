package com.dataexpo.autogate.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.model.gate.ReportData;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CardService implements GateObserver {
    private static final String TAG = CardService.class.getSimpleName();

    private CardService() {};

    private static class HolderClass {
        private static final CardService instance = new CardService();
    }
    /**
     * 单例模式
     */
    public static CardService getInstance() {
        return CardService.HolderClass.instance;
    }

    @Override
    public void responseData(ReportData mReports) {
        //Log.i(TAG, "responseData");
        //save record
        if (mReports == null) {
            return;
        }

        insert(mReports.getNumber(), mReports.getDirection(), Utils.timeNowLong());
    }

    @Override
    public void responseStatus(int status) {

    }

    /**
     * 添加数据
     */
    public long insert(String number, int direction, String time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", number);
        contentValues.put("direction", direction);
        contentValues.put("time", time);
        return DBUtils.getInstance().insert(DBUtils.TABLE_CARD_RECORD, null, contentValues);
    }

    /**
     * 查询全部通过记录
     * 返回List
     */
    public ArrayList<ReportData> listAll() {
        ArrayList<ReportData> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_CARD_RECORD, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String number = cursor.getString(cursor.getColumnIndex("number"));
            int direction = cursor.getInt(cursor.getColumnIndex("direction"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            list.add(new ReportData(id, number, direction, time));
        }

        cursor.close();
        return list;
    }

    /**
     * 查询全部用户数据
     * 返回List
     */
    public ArrayList<ReportData> queryByNumber(String number) {
        ArrayList<ReportData> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().findBy(DBUtils.TABLE_CARD_RECORD, "number", number);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String n = cursor.getString(cursor.getColumnIndex("number"));
            int direction = cursor.getInt(cursor.getColumnIndex("direction"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            list.add(new ReportData(id, n, direction, time));
        }

        cursor.close();
        return list;
    }
}
