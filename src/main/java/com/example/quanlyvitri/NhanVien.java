package com.example.quanlyvitri;

public class NhanVien {
    private int id;
    private String maNV, tenNV, ngaySinh, queQuan, duongDanAnh;
    private int viTriId;

    public NhanVien(int id, String maNV, String tenNV,
                    String ngaySinh, String queQuan,
                    String duongDanAnh, int viTriId) {
        this.id         = id;
        this.maNV       = maNV;
        this.tenNV      = tenNV;
        this.ngaySinh   = ngaySinh;
        this.queQuan    = queQuan;
        this.duongDanAnh = duongDanAnh;
        this.viTriId    = viTriId;
    }

    public int getId()              { return id; }
    public String getMaNV()         { return maNV; }
    public String getTenNV()        { return tenNV; }
    public String getNgaySinh()     { return ngaySinh != null ? ngaySinh : ""; }
    public String getQueQuan()      { return queQuan != null ? queQuan : ""; }
    public String getDuongDanAnh()  { return duongDanAnh != null ? duongDanAnh : ""; }
    public int getViTriId()         { return viTriId; }

    public void setId(int id)               { this.id = id; }
    public void setMaNV(String maNV)        { this.maNV = maNV; }
    public void setTenNV(String tenNV)      { this.tenNV = tenNV; }
    public void setNgaySinh(String s)       { this.ngaySinh = s; }
    public void setQueQuan(String s)        { this.queQuan = s; }
    public void setDuongDanAnh(String s)    { this.duongDanAnh = s; }
    public void setViTriId(int viTriId)     { this.viTriId = viTriId; }

    @Override
    public String toString() { return tenNV + " (" + maNV + ")"; }
}