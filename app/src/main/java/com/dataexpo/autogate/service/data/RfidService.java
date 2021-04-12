package com.dataexpo.autogate.service.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.GateObserver;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.RfidVo;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;

import java.util.ArrayList;

public class RfidService {
    private static final String TAG = RfidService.class.getSimpleName();

    private RfidService() {};

    private static class HolderClass {
        private static final RfidService instance = new RfidService();
    }
    /**
     * 单例模式
     */
    public static RfidService getInstance() {
        return RfidService.HolderClass.instance;
    }

    /**
     * 添加数据
     */
    public long insert(String ip, String port, int name, String remark) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("ip", ip);
        contentValues.put("port", port);
        contentValues.put("name", name);
        contentValues.put("remark", remark);
        return DBUtils.getInstance().insert(DBUtils.TABLE_RFID_GATE, null, contentValues);
    }

    public long insert(Rfid rfid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("ip", rfid.getIp());
        contentValues.put("port", rfid.getPort());
        contentValues.put("name", rfid.getName());
        contentValues.put("remark", rfid.getRemark());
        return DBUtils.getInstance().insert(DBUtils.TABLE_RFID_GATE, null, contentValues);
    }

    /**
     * 查询全部通过记录
     * 返回List
     */
    public ArrayList<Rfid> listAll() {
        ArrayList<Rfid> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().listAll(DBUtils.TABLE_RFID_GATE, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String ip = cursor.getString(cursor.getColumnIndex("ip"));
            String port = cursor.getString(cursor.getColumnIndex("port"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String remark = cursor.getString(cursor.getColumnIndex("remark"));
            list.add(new Rfid(id, ip, port, name, remark));
        }

        cursor.close();
        return list;
    }

    public ArrayList<Rfid> limitList(int start, int size) {
        ArrayList<Rfid> list = new ArrayList<>();
        Cursor cursor = DBUtils.getInstance().limitWith(DBUtils.TABLE_RFID_GATE, start, size);
        while (cursor.moveToNext()) {
            Rfid rfid = resolve(cursor);
            if (rfid != null) {
                list.add(rfid);
            }
        }
        cursor.close();
        return list;
    }

    /**
     * 修改配置
     * @param rfid
     */
    public void updateById(RfidVo rfid) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("ip", rfid.getIp());
        contentValues.put("port", rfid.getPort());
        contentValues.put("name", rfid.getName());
        contentValues.put("remark", rfid.getRemark());
        DBUtils.getInstance().update(DBUtils.TABLE_RFID_GATE,
                contentValues, "id = ?", new String[]{rfid.getId()+ ""});
    }

    private Rfid resolve(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Rfid rfid = new Rfid();
        rfid.setId(cursor.getInt(cursor.getColumnIndex("id")));
        rfid.setIp(cursor.getString(cursor.getColumnIndex("ip")));
        rfid.setPort(cursor.getString(cursor.getColumnIndex("port")));
        rfid.setName(cursor.getString(cursor.getColumnIndex("name")));
        rfid.setRemark(cursor.getString(cursor.getColumnIndex("remark")));

        return rfid;
    }

}
