package com.example.quanlyvitri;

public class ViTri {
    private int id;
    private String maVT, tenVT;
    private double mucLuong;
    private int soNhanLuc; // MỚI

    public ViTri(int id, String maVT, String tenVT, double mucLuong) {
        this.id = id;
        this.maVT = maVT;
        this.tenVT = tenVT;
        this.mucLuong = mucLuong;
        this.soNhanLuc = 1;
    }

    // Constructor đầy đủ
    public ViTri(int id, String maVT, String tenVT, double mucLuong, int soNhanLuc) {
        this(id, maVT, tenVT, mucLuong);
        this.soNhanLuc = soNhanLuc;
    }

    public int getId() { return id; }
    public String getMaVT() { return maVT; }
    public String getTenVT() { return tenVT; }
    public double getMucLuong() { return mucLuong; }
    public int getSoNhanLuc() { return soNhanLuc; }

    public void setId(int id) { this.id = id; }
    public void setMaVT(String maVT) { this.maVT = maVT; }
    public void setTenVT(String tenVT) { this.tenVT = tenVT; }
    public void setMucLuong(double mucLuong) { this.mucLuong = mucLuong; }
    public void setSoNhanLuc(int soNhanLuc) { this.soNhanLuc = soNhanLuc; }

    @Override
    public String toString() { return tenVT + " (" + maVT + ")"; }
}