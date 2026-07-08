package com.example.quanlyvitri;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LuongDAO {
    private DatabaseHelper dbHelper;
    private static final double THUONG_CHUYEN_CAN = 300000;

    public LuongDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ══ Tính số ngày làm việc chuẩn (Thứ 2→6) trong tháng ══
    public int ngayChuanTrongThang(int thang, int nam) {
        Calendar cal = Calendar.getInstance();
        cal.set(nam, thang - 1, 1);
        int soNgay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int count = 0;
        for (int i = 1; i <= soNgay; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) count++;
        }
        return count;
    }

    // ══ TÍNH LƯƠNG cho tất cả nhân viên trong 1 tháng ══
    public void tinhLuongThang(int thang, int nam) {
        int ngayChuan = ngayChuanTrongThang(thang, nam);
        String patternThang = String.format("%%/%02d/%d", thang, nam);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursorNV = db.rawQuery(
                "SELECT nv." + DatabaseHelper.NV_ID
                        + ", COALESCE(vt." + DatabaseHelper.VT_LUONG + ", 0)"
                        + " FROM " + DatabaseHelper.TABLE_NHAN_VIEN + " nv"
                        + " LEFT JOIN " + DatabaseHelper.TABLE_VI_TRI + " vt"
                        + " ON nv." + DatabaseHelper.NV_VI_TRI_ID + " = vt." + DatabaseHelper.VT_ID,
                null);

        db.beginTransaction();
        try {
            while (cursorNV.moveToNext()) {
                int nvId = cursorNV.getInt(0);
                double luongVT = cursorNV.getDouble(1);
                double luongMotNgay = ngayChuan > 0 ? luongVT / ngayChuan : 0;

                // Lấy từng ngày chấm công trong tháng
                Cursor cursorCC = db.rawQuery(
                        "SELECT " + DatabaseHelper.CC_NGAY + ", " + DatabaseHelper.CC_TRANG_THAI
                                + " FROM " + DatabaseHelper.TABLE_CHAM_CONG
                                + " WHERE " + DatabaseHelper.CC_NHAN_VIEN_ID + " = ?"
                                + " AND " + DatabaseHelper.CC_NGAY + " LIKE ?",
                        new String[]{String.valueOf(nvId), patternThang});

                double tongLuong = 0;
                int coMatThuong = 0, nghiPhep = 0, vangMat = 0, nghiLe = 0;
                int tangCaT7CN = 0, tangCaLe = 0;

                while (cursorCC.moveToNext()) {
                    String ngay = cursorCC.getString(0);
                    int tt = cursorCC.getInt(1);
                    boolean cuoiTuan = laCuoiTuan(ngay);
                    boolean ngayLe = laNgayLeTrongDB(db, ngay);

                    switch (tt) {
                        case 1: // Có mặt
                            if (ngayLe) {
                                tongLuong += luongMotNgay * 3;
                                tangCaLe++;
                            } else if (cuoiTuan) {
                                tongLuong += luongMotNgay * 2;
                                tangCaT7CN++;
                            } else {
                                tongLuong += luongMotNgay;
                                coMatThuong++;
                            }
                            break;
                        case 2: // Nghỉ phép — tính 1 ngày công
                            tongLuong += luongMotNgay;
                            nghiPhep++;
                            break;
                        case 4: // Nghỉ lễ (off) — tính 1 ngày công + giữ chuyên cần
                            tongLuong += luongMotNgay;
                            nghiLe++;
                            break;
                        case 3: // Vắng
                            vangMat++;
                            break;
                    }
                }
                cursorCC.close();

                // Ngày công thực (không tính tăng ca T7/CN vì ngoài ngày chuẩn)
                int ngayCongThuc = coMatThuong + nghiPhep + nghiLe;

                // Thưởng chuyên cần: đủ ngày chuẩn + không nghỉ phép + không vắng
                double thuong = ((coMatThuong + nghiLe + tangCaLe) >= ngayChuan
                        && nghiPhep == 0 && vangMat == 0)
                        ? THUONG_CHUYEN_CAN : 0;

                double thucNhan = Math.round(tongLuong) + thuong;

                // Lưu
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.BL_NHAN_VIEN_ID, nvId);
                values.put(DatabaseHelper.BL_THANG, thang);
                values.put(DatabaseHelper.BL_NAM, nam);
                values.put(DatabaseHelper.BL_NGAY_CONG_THUC, ngayCongThuc);
                values.put(DatabaseHelper.BL_NGHI_PHEP, nghiPhep);
                values.put(DatabaseHelper.BL_VANG, vangMat);
                values.put(DatabaseHelper.BL_LUONG_CO_BAN, luongVT);
                values.put(DatabaseHelper.BL_NGAY_CHUAN, ngayChuan);
                values.put(DatabaseHelper.BL_THUONG_CHUYEN_CAN, thuong);
                values.put(DatabaseHelper.BL_LUONG_THUC_NHAN, thucNhan);

                int rows = db.update(DatabaseHelper.TABLE_BANG_LUONG, values,
                        DatabaseHelper.BL_NHAN_VIEN_ID + " = ? AND "
                                + DatabaseHelper.BL_THANG + " = ? AND "
                                + DatabaseHelper.BL_NAM + " = ?",
                        new String[]{String.valueOf(nvId), String.valueOf(thang), String.valueOf(nam)});
                if (rows == 0) {
                    db.insert(DatabaseHelper.TABLE_BANG_LUONG, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            cursorNV.close();
            db.endTransaction();
            db.close();
        }
    }

    // ══ Lấy danh sách lương đã tính — JOIN với NV + VT ══
    public List<BangLuong> layDanhSach(int thang, int nam) {
        List<BangLuong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String patternNam = "%/" + nam;

        Cursor cursor = db.rawQuery(
                "SELECT bl.*, nv." + DatabaseHelper.NV_TEN + ", nv." + DatabaseHelper.NV_MA
                        + ", COALESCE(vt." + DatabaseHelper.VT_TEN + ", 'Chưa gán') AS ten_vt"
                        + " FROM " + DatabaseHelper.TABLE_BANG_LUONG + " bl"
                        + " JOIN " + DatabaseHelper.TABLE_NHAN_VIEN + " nv"
                        + " ON bl." + DatabaseHelper.BL_NHAN_VIEN_ID + " = nv." + DatabaseHelper.NV_ID
                        + " LEFT JOIN " + DatabaseHelper.TABLE_VI_TRI + " vt"
                        + " ON nv." + DatabaseHelper.NV_VI_TRI_ID + " = vt." + DatabaseHelper.VT_ID
                        + " WHERE bl." + DatabaseHelper.BL_THANG + " = ? AND bl." + DatabaseHelper.BL_NAM + " = ?"
                        + " ORDER BY nv." + DatabaseHelper.NV_MA + " ASC",
                new String[]{String.valueOf(thang), String.valueOf(nam)});

        while (cursor.moveToNext()) {
            BangLuong bl = new BangLuong();
            bl.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_ID)));
            bl.setNhanVienId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_NHAN_VIEN_ID)));
            bl.setThang(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_THANG)));
            bl.setNam(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_NAM)));
            bl.setNgayCongThuc(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_NGAY_CONG_THUC)));
            bl.setNghiPhep(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_NGHI_PHEP)));
            bl.setVang(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_VANG)));
            bl.setLuongCoBan(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_LUONG_CO_BAN)));
            bl.setNgayChuan(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_NGAY_CHUAN)));
            bl.setThuongChuyenCan(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_THUONG_CHUYEN_CAN)));
            bl.setLuongThucNhan(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.BL_LUONG_THUC_NHAN)));
            bl.setTenNV(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_TEN)));
            bl.setMaNV(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NV_MA)));
            bl.setTenViTri(cursor.getString(cursor.getColumnIndexOrThrow("ten_vt")));
            bl.setCoMat(bl.getNgayCongThuc() - bl.getNghiPhep());

            // Tính phép đã dùng cả năm
            Cursor cPhep = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAM_CONG
                            + " WHERE " + DatabaseHelper.CC_NHAN_VIEN_ID + " = ?"
                            + " AND " + DatabaseHelper.CC_TRANG_THAI + " = 2"
                            + " AND " + DatabaseHelper.CC_NGAY + " LIKE ?",
                    new String[]{String.valueOf(bl.getNhanVienId()), patternNam});
            if (cPhep.moveToFirst()) bl.setPhepDaDungNam(cPhep.getInt(0));
            cPhep.close();

            list.add(bl);
        }
        cursor.close();
        db.close();
        return list;
    }

    // ══ Kiểm tra đã tính lương chưa ══
    public boolean daTinhLuong(int thang, int nam) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BANG_LUONG
                        + " WHERE " + DatabaseHelper.BL_THANG + " = ? AND " + DatabaseHelper.BL_NAM + " = ?",
                new String[]{String.valueOf(thang), String.valueOf(nam)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count > 0;
    }

    // ══ Xóa lương khi xóa nhân viên ══
    public void xoaTheoNhanVien(int nhanVienId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BANG_LUONG,
                DatabaseHelper.BL_NHAN_VIEN_ID + " = ?",
                new String[]{String.valueOf(nhanVienId)});
        db.close();
    }
    private boolean laCuoiTuan(String ngay) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy",
                    java.util.Locale.getDefault());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(sdf.parse(ngay));
            int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
            return dow == java.util.Calendar.SATURDAY || dow == java.util.Calendar.SUNDAY;
        } catch (Exception e) { return false; }
    }

    private boolean laNgayLeTrongDB(SQLiteDatabase db, String ngay) {
        Cursor c = db.query(DatabaseHelper.TABLE_NGAY_NGHI_LE, null,
                DatabaseHelper.NL_NGAY + " = ?", new String[]{ngay},
                null, null, null);
        boolean result = c.getCount() > 0;
        c.close();
        return result;
    }
}