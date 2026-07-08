package com.example.quanlyvitri;

public class ChamCong {
    private int id;
    private int nhanVienId;
    private String ngay;       // dd/MM/yyyy
    private int trangThai;     // 1=có mặt, 2=nghỉ phép, 3=vắng

    public ChamCong() {}

    public ChamCong(int nhanVienId, String ngay, int trangThai) {
        this.nhanVienId = nhanVienId;
        this.ngay = ngay;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(int nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    // Tiện ích: chuyển số → tên hiển thị
    public String getTenTrangThai() {
        switch (trangThai) {
            case 1: return "Có mặt";
            case 2: return "Nghỉ phép";
            case 3: return "Vắng";
            case 4: return "Nghỉ lễ";    // ← THÊM
            default: return "Chưa chấm";
        }
    }
}