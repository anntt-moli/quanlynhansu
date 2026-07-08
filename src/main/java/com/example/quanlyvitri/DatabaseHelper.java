package com.example.quanlyvitri;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quanlyvitri.db";
    private static final int DATABASE_VERSION = 5;

    // ── Bảng cũ ──
    public static final String TABLE_NHAN_VIEN = "nhan_vien";
    public static final String TABLE_VI_TRI    = "vi_tri";

    // ── 3 bảng MỚI ──
    public static final String TABLE_HOP_DONG  = "hop_dong";
    public static final String TABLE_CHAM_CONG = "cham_cong";
    public static final String TABLE_BANG_LUONG = "bang_luong";

    // Cột bảng NHAN_VIEN (giữ nguyên)
    public static final String NV_ID        = "id";
    public static final String NV_MA        = "ma_nv";
    public static final String NV_TEN       = "ten_nv";
    public static final String NV_NGAY_SINH = "ngay_sinh";
    public static final String NV_QUE_QUAN  = "que_quan";
    public static final String NV_ANH       = "duong_dan_anh";
    public static final String NV_VI_TRI_ID = "vi_tri_id";

    // Cột bảng VI_TRI (giữ nguyên)
    public static final String VT_ID    = "id";
    public static final String VT_MA    = "ma_vi_tri";
    public static final String VT_TEN   = "ten_vi_tri";
    public static final String VT_LUONG = "muc_luong";
    // Cột mới bảng VI_TRI
    public static final String VT_SO_NHAN_LUC = "so_nhan_luc";


    // Cột bảng HOP_DONG (MỚI)
    public static final String HD_ID           = "id";
    public static final String HD_NHAN_VIEN_ID = "nhan_vien_id";
    public static final String HD_LOAI         = "loai_hop_dong";   // thu_viec, chinh_thuc, thoi_vu
    public static final String HD_NGAY_BAT_DAU = "ngay_bat_dau";
    public static final String HD_NGAY_KET_THUC = "ngay_ket_thuc"; // NULL = không xác định
    public static final String HD_GHI_CHU      = "ghi_chu";

    // Cột bảng CHAM_CONG (MỚI)
    public static final String CC_ID           = "id";
    public static final String CC_NHAN_VIEN_ID = "nhan_vien_id";
    public static final String CC_NGAY         = "ngay";            // dd/MM/yyyy
    public static final String CC_TRANG_THAI   = "trang_thai";      // 1=có mặt, 2=nghỉ phép, 3=vắng

    // Cột bảng BANG_LUONG (MỚI)
    public static final String BL_ID              = "id";
    public static final String BL_NHAN_VIEN_ID    = "nhan_vien_id";
    public static final String BL_THANG           = "thang";
    public static final String BL_NAM             = "nam";
    public static final String BL_NGAY_CONG_THUC  = "ngay_cong_thuc";
    public static final String BL_NGHI_PHEP       = "nghi_phep";
    public static final String BL_VANG            = "vang";
    public static final String BL_LUONG_CO_BAN    = "luong_co_ban";
    public static final String BL_NGAY_CHUAN      = "ngay_chuan";
    public static final String BL_THUONG_CHUYEN_CAN = "thuong_chuyen_can";
    public static final String BL_LUONG_THUC_NHAN = "luong_thuc_nhan";

    // Bảng NGAY_NGHI_LE (MỚI — cho tính năng tăng ca)
    public static final String TABLE_NGAY_NGHI_LE = "ngay_nghi_le";
    public static final String NL_ID  = "id";
    public static final String NL_NGAY = "ngay"; // dd/MM/yyyy

    // Hằng số nghiệp vụ
    public static final int PHEP_NAM_MAC_DINH = 12; // 12 ngày phép/năm

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ── Bảng VI_TRI (tạo trước vì NHAN_VIEN phụ thuộc) ──
        db.execSQL("CREATE TABLE " + TABLE_VI_TRI + " ("
                + VT_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VT_MA    + " TEXT UNIQUE NOT NULL, "
                + VT_TEN   + " TEXT NOT NULL, "
                + VT_LUONG + " REAL NOT NULL, "
                + VT_SO_NHAN_LUC + " INTEGER DEFAULT 1)");

        // ── Bảng NHAN_VIEN ──
        db.execSQL("CREATE TABLE " + TABLE_NHAN_VIEN + " ("
                + NV_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NV_MA        + " TEXT UNIQUE NOT NULL, "
                + NV_TEN       + " TEXT NOT NULL, "
                + NV_NGAY_SINH + " TEXT, "
                + NV_QUE_QUAN  + " TEXT, "
                + NV_ANH       + " TEXT, "
                + NV_VI_TRI_ID + " INTEGER, "
                + "FOREIGN KEY(" + NV_VI_TRI_ID + ") REFERENCES "
                + TABLE_VI_TRI + "(" + VT_ID + "))");

        // ── Bảng HOP_DONG (MỚI) ──
        taoTableHopDong(db);

        // ── Bảng CHAM_CONG (MỚI) ──
        taoTableChamCong(db);

        // ── Bảng BANG_LUONG (MỚI) ──
        taoTableBangLuong(db);

        taoTableNgayNghiLe(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp dần — giữ lại dữ liệu cũ
        if (oldVersion < 4) {
            taoTableHopDong(db);
            taoTableChamCong(db);
            taoTableBangLuong(db);
        }
        // Khi cần nâng cấp tiếp trong tương lai, thêm: if (oldVersion < 5) { ... }
        if (oldVersion < 5) {
            // Thêm cột số nhân lực vào bảng VI_TRI
            db.execSQL("ALTER TABLE " + TABLE_VI_TRI
                    + " ADD COLUMN " + VT_SO_NHAN_LUC + " INTEGER DEFAULT 1");
            // Tạo bảng ngày nghỉ lễ
            taoTableNgayNghiLe(db);
        }
    }

    // ── Tách riêng câu lệnh tạo bảng để dùng chung cho cả onCreate và onUpgrade ──

    private void taoTableHopDong(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HOP_DONG + " ("
                + HD_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HD_NHAN_VIEN_ID + " INTEGER NOT NULL, "
                + HD_LOAI         + " TEXT NOT NULL, "
                + HD_NGAY_BAT_DAU + " TEXT NOT NULL, "
                + HD_NGAY_KET_THUC + " TEXT, "
                + HD_GHI_CHU      + " TEXT, "
                + "FOREIGN KEY(" + HD_NHAN_VIEN_ID + ") REFERENCES "
                + TABLE_NHAN_VIEN + "(" + NV_ID + "))");
    }

    private void taoTableChamCong(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAM_CONG + " ("
                + CC_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CC_NHAN_VIEN_ID + " INTEGER NOT NULL, "
                + CC_NGAY         + " TEXT NOT NULL, "
                + CC_TRANG_THAI   + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + CC_NHAN_VIEN_ID + ") REFERENCES "
                + TABLE_NHAN_VIEN + "(" + NV_ID + "), "
                + "UNIQUE(" + CC_NHAN_VIEN_ID + ", " + CC_NGAY + "))");
    }

    private void taoTableBangLuong(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BANG_LUONG + " ("
                + BL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BL_NHAN_VIEN_ID    + " INTEGER NOT NULL, "
                + BL_THANG           + " INTEGER NOT NULL, "
                + BL_NAM             + " INTEGER NOT NULL, "
                + BL_NGAY_CONG_THUC  + " INTEGER DEFAULT 0, "
                + BL_NGHI_PHEP       + " INTEGER DEFAULT 0, "
                + BL_VANG            + " INTEGER DEFAULT 0, "
                + BL_LUONG_CO_BAN    + " REAL DEFAULT 0, "
                + BL_NGAY_CHUAN      + " INTEGER DEFAULT 22, "
                + BL_THUONG_CHUYEN_CAN + " REAL DEFAULT 0, "
                + BL_LUONG_THUC_NHAN + " REAL DEFAULT 0, "
                + "FOREIGN KEY(" + BL_NHAN_VIEN_ID + ") REFERENCES "
                + TABLE_NHAN_VIEN + "(" + NV_ID + "), "
                + "UNIQUE(" + BL_NHAN_VIEN_ID + ", " + BL_THANG + ", " + BL_NAM + "))");
    }

    private void taoTableNgayNghiLe(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NGAY_NGHI_LE + " ("
                + NL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NL_NGAY + " TEXT UNIQUE NOT NULL)");
    }

    // ── Tạo mã tự động (giữ nguyên) ──

    public String taoMaNhanVien(SQLiteDatabase db) {
        android.database.Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NHAN_VIEN, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return String.format("NV%03d", count + 1);
    }

    public String taoMaViTri(SQLiteDatabase db) {
        android.database.Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_VI_TRI, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return String.format("VT%03d", count + 1);
    }
}