package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class HopDongDAO {
    private DatabaseHelper dbHelper;

    public HopDongDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ── Thêm hợp đồng — trả về id mới ──
    public long them(HopDong hd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HD_NHAN_VIEN_ID, hd.getNhanVienId());
        values.put(DatabaseHelper.HD_LOAI, hd.getLoaiHopDong());
        values.put(DatabaseHelper.HD_NGAY_BAT_DAU, hd.getNgayBatDau());
        values.put(DatabaseHelper.HD_NGAY_KET_THUC, hd.getNgayKetThuc());
        values.put(DatabaseHelper.HD_GHI_CHU, hd.getGhiChu());
        long id = db.insert(DatabaseHelper.TABLE_HOP_DONG, null, values);
        db.close();
        return id;
    }

    // ── Sửa hợp đồng ──
    public int sua(HopDong hd) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HD_LOAI, hd.getLoaiHopDong());
        values.put(DatabaseHelper.HD_NGAY_BAT_DAU, hd.getNgayBatDau());
        values.put(DatabaseHelper.HD_NGAY_KET_THUC, hd.getNgayKetThuc());
        values.put(DatabaseHelper.HD_GHI_CHU, hd.getGhiChu());
        int rows = db.update(DatabaseHelper.TABLE_HOP_DONG, values,
                DatabaseHelper.HD_ID + " = ?",
                new String[]{String.valueOf(hd.getId())});
        db.close();
        return rows;
    }

    // ── Xóa 1 hợp đồng theo id ──
    public int xoa(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_HOP_DONG,
                DatabaseHelper.HD_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // ── Xóa tất cả hợp đồng của 1 nhân viên (dùng khi xóa nhân viên) ──
    public int xoaTheoNhanVien(int nhanVienId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_HOP_DONG,
                DatabaseHelper.HD_NHAN_VIEN_ID + " = ?",
                new String[]{String.valueOf(nhanVienId)});
        db.close();
        return rows;
    }

    // ── Lấy tất cả hợp đồng của 1 nhân viên — sắp xếp mới nhất trước ──
    public List<HopDong> layTheoNhanVien(int nhanVienId) {
        List<HopDong> danhSach = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HOP_DONG,
                null,
                DatabaseHelper.HD_NHAN_VIEN_ID + " = ?",
                new String[]{String.valueOf(nhanVienId)},
                null, null,
                DatabaseHelper.HD_NGAY_BAT_DAU + " DESC");

        while (cursor.moveToNext()) {
            danhSach.add(cursorToHopDong(cursor));
        }
        cursor.close();
        db.close();
        return danhSach;
    }

    // ── Lấy 1 hợp đồng theo id ──
    public HopDong layTheoId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HOP_DONG,
                null,
                DatabaseHelper.HD_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        HopDong hd = null;
        if (cursor.moveToFirst()) {
            hd = cursorToHopDong(cursor);
        }
        cursor.close();
        db.close();
        return hd;
    }

    // ── Lấy hợp đồng còn hiệu lực gần nhất của 1 nhân viên ──
    // Dùng để hiển thị trạng thái trên danh sách nhân viên (Giai đoạn 3)
    public HopDong layHopDongHieuLuc(int nhanVienId) {
        List<HopDong> tatCa = layTheoNhanVien(nhanVienId);
        if (tatCa.isEmpty()) return null;

        // Ưu tiên: hợp đồng còn hạn hoặc không thời hạn
        for (HopDong hd : tatCa) {
            if (hd.getSoNgayConLai() >= 0) {
                return hd; // còn hạn hoặc không thời hạn
            }
        }
        // Nếu tất cả đã hết hạn → trả về cái mới nhất
        return tatCa.get(0);
    }

    // ── Chuyển Cursor → đối tượng HopDong ──
    private HopDong cursorToHopDong(Cursor cursor) {
        HopDong hd = new HopDong();
        hd.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.HD_ID)));
        hd.setNhanVienId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.HD_NHAN_VIEN_ID)));
        hd.setLoaiHopDong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HD_LOAI)));
        hd.setNgayBatDau(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HD_NGAY_BAT_DAU)));

        int colKetThuc = cursor.getColumnIndexOrThrow(DatabaseHelper.HD_NGAY_KET_THUC);
        hd.setNgayKetThuc(cursor.isNull(colKetThuc) ? null : cursor.getString(colKetThuc));

        int colGhiChu = cursor.getColumnIndexOrThrow(DatabaseHelper.HD_GHI_CHU);
        hd.setGhiChu(cursor.isNull(colGhiChu) ? null : cursor.getString(colGhiChu));

        return hd;
    }
}