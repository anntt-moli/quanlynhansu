package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public NhanVienDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }


    private NhanVien fromCursor(Cursor cursor) {
        return new NhanVien(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_MA)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_TEN)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_NGAY_SINH)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_QUE_QUAN)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_ANH)),
                cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_VI_TRI_ID))
                        ? -1
                        : cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_VI_TRI_ID))
        );
    }

    //  THÊM
    public long them(String tenNV, String ngaySinh, String queQuan,
                     int viTriId, String duongDanAnh) {
        String maNV = dbHelper.taoMaNhanVien(db);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NV_MA, maNV);
        values.put(DatabaseHelper.NV_TEN, tenNV);
        values.put(DatabaseHelper.NV_NGAY_SINH, ngaySinh);
        values.put(DatabaseHelper.NV_QUE_QUAN, queQuan);
        values.put(DatabaseHelper.NV_ANH, duongDanAnh);
        if (viTriId != -1)
            values.put(DatabaseHelper.NV_VI_TRI_ID, viTriId);
        else
            values.putNull(DatabaseHelper.NV_VI_TRI_ID);
        return db.insert(DatabaseHelper.TABLE_NHAN_VIEN, null, values);
    }

    // SỬA
    public int sua(int id, String maNV, String tenNV,
                   String ngaySinh, String queQuan) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NV_MA, maNV);
        values.put(DatabaseHelper.NV_TEN, tenNV);
        values.put(DatabaseHelper.NV_NGAY_SINH, ngaySinh);
        values.put(DatabaseHelper.NV_QUE_QUAN, queQuan);
        return db.update(DatabaseHelper.TABLE_NHAN_VIEN, values,
                DatabaseHelper.NV_ID + "=?", new String[]{String.valueOf(id)});
    }

    // CẬP NHẬT VỊ TRÍ
    public int capNhatViTri(int nhanVienId, int viTriId) {
        ContentValues values = new ContentValues();
        if (viTriId != -1)
            values.put(DatabaseHelper.NV_VI_TRI_ID, viTriId);
        else
            values.putNull(DatabaseHelper.NV_VI_TRI_ID);
        return db.update(DatabaseHelper.TABLE_NHAN_VIEN, values,
                DatabaseHelper.NV_ID + "=?",
                new String[]{String.valueOf(nhanVienId)});
    }

    // CẬP NHẬT ẢNH
    public int capNhatAnh(int id, String duongDanAnh) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NV_ANH, duongDanAnh);
        return db.update(DatabaseHelper.TABLE_NHAN_VIEN, values,
                DatabaseHelper.NV_ID + "=?", new String[]{String.valueOf(id)});
    }

    // XÓA
    public int xoa(int id) {
        return db.delete(DatabaseHelper.TABLE_NHAN_VIEN,
                DatabaseHelper.NV_ID + "=?", new String[]{String.valueOf(id)});
    }

    // LẤY TẤT CẢ
    public List<NhanVien> layTatCa() {
        List<NhanVien> danhSach = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NHAN_VIEN,
                null, null, null, null, null,
                DatabaseHelper.NV_MA + " ASC");
        if (cursor.moveToFirst()) {
            do { danhSach.add(fromCursor(cursor)); }
            while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // LẤY THEO ID
    public NhanVien layTheoId(int id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_NHAN_VIEN,
                null, DatabaseHelper.NV_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst()) {
            NhanVien nv = fromCursor(cursor);
            cursor.close();
            return nv;
        }
        cursor.close();
        return null;
    }

    // LẤY THEO VỊ TRÍ
    public List<NhanVien> layTheoViTri(int viTriId) {
        List<NhanVien> danhSach = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NHAN_VIEN,
                null, DatabaseHelper.NV_VI_TRI_ID + "=?",
                new String[]{String.valueOf(viTriId)},
                null, null, DatabaseHelper.NV_MA + " ASC");
        if (cursor.moveToFirst()) {
            do { danhSach.add(fromCursor(cursor)); }
            while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // LẤY nhân viên chưa gán vị trí
    public List<NhanVien> layNhanVienChuaGan() {
        List<NhanVien> danhSach = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NHAN_VIEN,
                null,
                DatabaseHelper.NV_VI_TRI_ID + " IS NULL",
                null, null, null, DatabaseHelper.NV_MA + " ASC");
        if (cursor.moveToFirst()) {
            do { danhSach.add(fromCursor(cursor)); }
            while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // LẤY NV LƯƠNG CAO (JOIN 2 bảng)
    public List<String> layNhanVienLuongCao() {
        List<String> danhSach = new ArrayList<>();
        String query = "SELECT nv.*, vt." + DatabaseHelper.VT_TEN
                + ", vt." + DatabaseHelper.VT_LUONG
                + " FROM " + DatabaseHelper.TABLE_NHAN_VIEN + " nv"
                + " INNER JOIN " + DatabaseHelper.TABLE_VI_TRI + " vt"
                + " ON nv." + DatabaseHelper.NV_VI_TRI_ID
                + " = vt." + DatabaseHelper.VT_ID
                + " WHERE vt." + DatabaseHelper.VT_LUONG + " > 10000000"
                + " ORDER BY nv." + DatabaseHelper.NV_MA + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String tenNV  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_TEN));
                String maNV   = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_MA));
                String tenVT  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_TEN));
                double luong  = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_LUONG));
                String luongFmt = java.text.NumberFormat
                        .getNumberInstance(java.util.Locale.US).format(luong) + " đ";
                danhSach.add(tenNV + "  |  " + maNV + "\n    → " + tenVT + ": " + luongFmt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // LẤY VỊ TRÍ CỦA 1 NHÂN VIÊN
    public ViTri layViTriCuaNhanVien(int nhanVienId) {
        String query = "SELECT vt.* FROM " + DatabaseHelper.TABLE_VI_TRI + " vt"
                + " INNER JOIN " + DatabaseHelper.TABLE_NHAN_VIEN + " nv"
                + " ON vt." + DatabaseHelper.VT_ID
                + " = nv." + DatabaseHelper.NV_VI_TRI_ID
                + " WHERE nv." + DatabaseHelper.NV_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(nhanVienId)});
        if (cursor.moveToFirst()) {
            ViTri vt = new ViTri(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_MA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_TEN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.VT_LUONG))
            );
            cursor.close();
            return vt;
        }
        cursor.close();
        return null;  // null = chưa có vị trí
    }



    public void close() { dbHelper.close(); }
}

