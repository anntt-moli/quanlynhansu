package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ViTriDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ViTriDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long them(String tenVT, double mucLuong, int soNhanLuc) {
        String maVT = dbHelper.taoMaViTri(db);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.VT_MA, maVT);
        values.put(DatabaseHelper.VT_TEN, tenVT);
        values.put(DatabaseHelper.VT_LUONG, mucLuong);
        values.put(DatabaseHelper.VT_SO_NHAN_LUC, soNhanLuc);
        return db.insert(DatabaseHelper.TABLE_VI_TRI, null, values);
    }

    public int sua(int id, String maVT, String tenVT, double mucLuong, int soNhanLuc) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.VT_MA, maVT);
        values.put(DatabaseHelper.VT_TEN, tenVT);
        values.put(DatabaseHelper.VT_LUONG, mucLuong);
        values.put(DatabaseHelper.VT_SO_NHAN_LUC, soNhanLuc);
        return db.update(DatabaseHelper.TABLE_VI_TRI, values,
                DatabaseHelper.VT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int soNhanVienTheoViTri(int viTriId) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NHAN_VIEN
                        + " WHERE " + DatabaseHelper.NV_VI_TRI_ID + " = ?",
                new String[]{String.valueOf(viTriId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int xoa(int id) {
        return db.delete(DatabaseHelper.TABLE_VI_TRI,
                DatabaseHelper.VT_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<ViTri> layTatCa() {
        List<ViTri> danhSach = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_VI_TRI,
                null, null, null, null, null, DatabaseHelper.VT_MA + " ASC");
        if (cursor.moveToFirst()) {
            do {
                danhSach.add(cursorToViTri(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    public List<ViTri> layLuongTren10Trieu() {
        List<ViTri> danhSach = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_VI_TRI,
                null, DatabaseHelper.VT_LUONG + " > ?",
                new String[]{"10000000"}, null, null, DatabaseHelper.VT_LUONG + " DESC");
        if (cursor.moveToFirst()) {
            do {
                danhSach.add(cursorToViTri(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    private ViTri cursorToViTri(Cursor cursor) {
        int snl = 1;
        int colSNL = cursor.getColumnIndex(DatabaseHelper.VT_SO_NHAN_LUC);
        if (colSNL >= 0 && !cursor.isNull(colSNL)) snl = cursor.getInt(colSNL);

        return new ViTri(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_MA)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_TEN)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_LUONG)),
                snl
        );
    }

    public void close() { dbHelper.close(); }
}