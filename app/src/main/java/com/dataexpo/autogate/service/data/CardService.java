package com.dataexpo.autogate.service.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;

import java.util.ArrayList;

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

        //当模式是普通模式时保存记录，如果是校验模式则由界面统一控制门控制和记录保存

        if (MainApplication.getInstance().getpModel() == 0) {
            insert(mReports.getNumber(), mReports.getDirection(), Utils.timeNowLong());
        }
    }

    @Override
    public void responseStatus(Rfid rfid) {

    }

    /**
     * 添加数据
     */
    public long insert(String number, int direction, String time) {
        Log.i(TAG, "insert 1  " + number);
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", number);
        contentValues.put("direction", direction);
        contentValues.put("time", time);
        contentValues.put("model", 0);
        return DBUtils.getInstance().insert(DBUtils.TABLE_CARD_RECORD, null, contentValues);
    }

    /**
     * 添加数据
     */
    public long insert(ReportData reportData) {
        Log.i(TAG, "insert 2  " + reportData.getNumber());
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", reportData.getNumber());
        contentValues.put("direction", reportData.getDirection());
        contentValues.put("time", reportData.getLtime());
        contentValues.put("model", reportData.getModel());
        contentValues.put("pid", reportData.getPid());
        contentValues.put("address", reportData.getAddress());
        contentValues.put("status", reportData.getStatus());
        contentValues.put("permissionid", reportData.getPermissionid());

        return DBUtils.getInstance().insert(DBUtils.TABLE_CARD_RECORD, null, contentValues);
    }

    /**
     * 获取全部闸机通过记录的总数
     * @return
     */
    public int countOfTotalGate() {
        return DBUtils.getInstance().count("select count(id) from " +
                DBUtils.TABLE_CARD_RECORD);
    }

    /**
     * 清空全部日志
     */
    public void clearAllData() {
        DBUtils.getInstance().delDataAll(DBUtils.TABLE_CARD_RECORD);
    }

    /**
     * 查询全部通过记录
     * 返回List
     */
    public ArrayList<ReportData> listAll() {
        ArrayList<ReportData> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_CARD_RECORD, null, null, null, null, null, "id desc");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String number = cursor.getString(cursor.getColumnIndex("number"));
            int direction = cursor.getInt(cursor.getColumnIndex("direction"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            int model = cursor.getInt(cursor.getColumnIndex("model"));
            int pid = cursor.getInt(cursor.getColumnIndex("pid"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            int status = cursor.getInt(cursor.getColumnIndex("status"));
            int permissionid = cursor.getInt(cursor.getColumnIndex("permissionid"));
            list.add(new ReportData(id, number, direction, time, model, pid, address, status, permissionid));
        }

        cursor.close();
        return list;
    }

    /**
     * 查询一个卡号的全部数据
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
            int model = cursor.getInt(cursor.getColumnIndex("model"));
            int pid = cursor.getInt(cursor.getColumnIndex("pid"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            int status = cursor.getInt(cursor.getColumnIndex("status"));
            int permissionid = cursor.getInt(cursor.getColumnIndex("permissionid"));
            list.add(new ReportData(id, number, direction, time, model, pid, address, status, permissionid));
        }

        cursor.close();
        return list;
    }
}
