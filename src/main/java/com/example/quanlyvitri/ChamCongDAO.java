package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.HashMap;
import java.util.Map;

public class ChamCongDAO {
    private DatabaseHelper dbHelper;

    public ChamCongDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ── Lưu 1 bản ghi — tự xử lý trùng (cùng nhân viên + cùng ngày) ──
    public void luu(int nhanVienId, String ngay, int trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.CC_NHAN_VIEN_ID, nhanVienId);
        values.put(DatabaseHelper.CC_NGAY, ngay);
        values.put(DatabaseHelper.CC_TRANG_THAI, trangThai);

        // Thử update trước, nếu chưa có thì insert
        int rows = db.update(DatabaseHelper.TABLE_CHAM_CONG, values,
                DatabaseHelper.CC_NHAN_VIEN_ID + " = ? AND " + DatabaseHelper.CC_NGAY + " = ?",
                new String[]{String.valueOf(nhanVienId), ngay});
        if (rows == 0) {
            db.insert(DatabaseHelper.TABLE_CHAM_CONG, null, values);
        }
        db.close();
    }

    // ── Lưu hàng loạt trong 1 transaction — nhanh hơn gọi luu() từng cái ──
    public void luuTatCa(String ngay, Map<Integer, Integer> danhSach) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<Integer, Integer> entry : danhSach.entrySet()) {
                int nhanVienId = entry.getKey();
                int trangThai = entry.getValue();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.CC_NHAN_VIEN_ID, nhanVienId);
                values.put(DatabaseHelper.CC_NGAY, ngay);
                values.put(DatabaseHelper.CC_TRANG_THAI, trangThai);

                int rows = db.update(DatabaseHelper.TABLE_CHAM_CONG, values,
                        DatabaseHelper.CC_NHAN_VIEN_ID + " = ? AND " + DatabaseHelper.CC_NGAY + " = ?",
                        new String[]{String.valueOf(nhanVienId), ngay});
                if (rows == 0) {
                    db.insert(DatabaseHelper.TABLE_CHAM_CONG, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // ── Lấy chấm công theo ngày — trả về Map: nhanVienId → trangThai ──
    public Map<Integer, Integer> layMapTheoNgay(String ngay) {
        Map<Integer, Integer> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CHAM_CONG, null,
                DatabaseHelper.CC_NGAY + " = ?",
                new String[]{ngay}, null, null, null);

        while (cursor.moveToNext()) {
            int nvId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CC_NHAN_VIEN_ID));
            int tt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CC_TRANG_THAI));
            map.put(nvId, tt);
        }
        cursor.close();
        db.close();
        return map;
    }

    // ── Đếm theo trạng thái cho 1 ngày ──
    public int dem(String ngay, int trangThai) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAM_CONG
                        + " WHERE " + DatabaseHelper.CC_NGAY + " = ? AND "
                        + DatabaseHelper.CC_TRANG_THAI + " = ?",
                new String[]{ngay, String.valueOf(trangThai)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    // ── Xóa tất cả chấm công của 1 nhân viên (khi xóa nhân viên) ──
    public void xoaTheoNhanVien(int nhanVienId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CHAM_CONG,
                DatabaseHelper.CC_NHAN_VIEN_ID + " = ?",
                new String[]{String.valueOf(nhanVienId)});
        db.close();
    }
    // ── Chấm hàng loạt: nhiều ngày × nhiều nhân viên ──
    public int chamHangLoat(java.util.Map<String, Integer> ngayVaTrangThai,
                            java.util.List<Integer> dsNvId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        db.beginTransaction();
        try {
            for (java.util.Map.Entry<String, Integer> entry : ngayVaTrangThai.entrySet()) {
                String ngay = entry.getKey();
                int trangThai = entry.getValue();
                for (int nvId : dsNvId) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.CC_NHAN_VIEN_ID, nvId);
                    values.put(DatabaseHelper.CC_NGAY, ngay);
                    values.put(DatabaseHelper.CC_TRANG_THAI, trangThai);
                    int rows = db.update(DatabaseHelper.TABLE_CHAM_CONG, values,
                            DatabaseHelper.CC_NHAN_VIEN_ID + " = ? AND " + DatabaseHelper.CC_NGAY + " = ?",
                            new String[]{String.valueOf(nvId), ngay});
                    if (rows == 0) db.insert(DatabaseHelper.TABLE_CHAM_CONG, null, values);
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return count;
    }
}