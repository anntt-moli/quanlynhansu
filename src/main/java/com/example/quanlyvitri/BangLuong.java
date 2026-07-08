package com.example.quanlyvitri;

public class BangLuong {
    // Các cột trong database
    private int id, nhanVienId, thang, nam;
    private int ngayCongThuc, nghiPhep, vang;
    private double luongCoBan, thuongChuyenCan, luongThucNhan;
    private int ngayChuan;

    // Thông tin bổ sung cho hiển thị (không lưu DB — lấy từ JOIN)
    private String tenNV, maNV, tenViTri;
    private int coMat;          // có mặt (để hiển thị chi tiết)
    private int phepDaDungNam;  // tổng phép đã dùng cả năm

    public BangLuong() {}

    // ── Getters / Setters ──
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(int nhanVienId) { this.nhanVienId = nhanVienId; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public int getNgayCongThuc() { return ngayCongThuc; }
    public void setNgayCongThuc(int ngayCongThuc) { this.ngayCongThuc = ngayCongThuc; }

    public int getNghiPhep() { return nghiPhep; }
    public void setNghiPhep(int nghiPhep) { this.nghiPhep = nghiPhep; }

    public int getVang() { return vang; }
    public void setVang(int vang) { this.vang = vang; }

    public double getLuongCoBan() { return luongCoBan; }
    public void setLuongCoBan(double luongCoBan) { this.luongCoBan = luongCoBan; }

    public int getNgayChuan() { return ngayChuan; }
    public void setNgayChuan(int ngayChuan) { this.ngayChuan = ngayChuan; }

    public double getThuongChuyenCan() { return thuongChuyenCan; }
    public void setThuongChuyenCan(double thuongChuyenCan) { this.thuongChuyenCan = thuongChuyenCan; }

    public double getLuongThucNhan() { return luongThucNhan; }
    public void setLuongThucNhan(double luongThucNhan) { this.luongThucNhan = luongThucNhan; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getTenViTri() { return tenViTri; }
    public void setTenViTri(String tenViTri) { this.tenViTri = tenViTri; }

    public int getCoMat() { return coMat; }
    public void setCoMat(int coMat) { this.coMat = coMat; }

    public int getPhepDaDungNam() { return phepDaDungNam; }
    public void setPhepDaDungNam(int phepDaDungNam) { this.phepDaDungNam = phepDaDungNam; }

    // ── Tiện ích tính toán ──
    public String getHeSoCong() {
        return ngayCongThuc + " / " + ngayChuan;
    }

    public int getPhepConLai() {
        return Math.max(0, DatabaseHelper.PHEP_NAM_MAC_DINH - phepDaDungNam);
    }
}