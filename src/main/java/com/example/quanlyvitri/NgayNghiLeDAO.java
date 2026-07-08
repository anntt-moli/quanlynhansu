package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NgayNghiLeDAO {
    private DatabaseHelper dbHelper;

    public NgayNghiLeDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void danhDau(String ngay) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NL_NGAY, ngay);
        db.insertWithOnConflict(DatabaseHelper.TABLE_NGAY_NGHI_LE, null,
                values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void boDanhDau(String ngay) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NGAY_NGHI_LE,
                DatabaseHelper.NL_NGAY + " = ?", new String[]{ngay});
        db.close();
    }

    public boolean laNgayLe(String ngay) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NGAY_NGHI_LE, null,
                DatabaseHelper.NL_NGAY + " = ?", new String[]{ngay},
                null, null, null);
        boolean ketQua = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return ketQua;
    }
}