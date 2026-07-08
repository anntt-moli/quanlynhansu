package com.example.quanlyvitri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HopDong {
    private int id;
    private int nhanVienId;
    private String loaiHopDong;   // thu_viec, chinh_thuc, thoi_vu
    private String ngayBatDau;    // dd/MM/yyyy
    private String ngayKetThuc;   // dd/MM/yyyy hoặc null (không xác định)
    private String ghiChu;

    // ── Constructor ──
    public HopDong() {}

    public HopDong(int nhanVienId, String loaiHopDong,
                   String ngayBatDau, String ngayKetThuc, String ghiChu) {
        this.nhanVienId = nhanVienId;
        this.loaiHopDong = loaiHopDong;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.ghiChu = ghiChu;
    }

    // ── Getter / Setter ──
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(int nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getLoaiHopDong() { return loaiHopDong; }
    public void setLoaiHopDong(String loaiHopDong) { this.loaiHopDong = loaiHopDong; }

    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    // ── Phương thức tiện ích — hiển thị tên loại hợp đồng ──
    public String getTenLoai() {
        if (loaiHopDong == null) return "";
        switch (loaiHopDong) {
            case "chinh_thuc": return "Chính thức";
            case "thu_viec":   return "Thử việc";
            case "thoi_vu":    return "Thời vụ";
            default: return loaiHopDong;
        }
    }

    // ── Tính số ngày còn lại đến khi hết hạn ──
    // Trả về: số dương = còn hạn, 0 = hết hạn hôm nay, số âm = đã hết hạn
    // Trả về Long.MAX_VALUE nếu không có ngày kết thúc (không thời hạn)
    public long getSoNgayConLai() {
        if (ngayKetThuc == null || ngayKetThuc.trim().isEmpty()) {
            return Long.MAX_VALUE; // không thời hạn
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date ngayKT = sdf.parse(ngayKetThuc);
            Date homNay = new Date();
            // Reset giờ về 00:00 để so sánh chính xác theo ngày
            String homNayStr = sdf.format(homNay);
            Date homNayReset = sdf.parse(homNayStr);
            long diffMs = ngayKT.getTime() - homNayReset.getTime();
            return TimeUnit.DAYS.convert(diffMs, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return 0;
        }
    }

    // ── Lấy text trạng thái để hiển thị ──
    // Trả về mảng: [0] = text, [1] = "xanh" / "vang" / "do"
    public String[] getTrangThai() {
        long soNgay = getSoNgayConLai();

        if (soNgay == Long.MAX_VALUE) {
            return new String[]{"Không thời hạn", "xanh"};
        } else if (soNgay < 0) {
            return new String[]{"Đã hết hạn", "do"};
        } else if (soNgay <= 30) {
            if (soNgay == 0) {
                return new String[]{"Hết hạn hôm nay", "do"};
            }
            return new String[]{"Còn " + soNgay + " ngày sẽ hết hạn", "vang"};
        } else {
            return new String[]{"Còn " + soNgay + " ngày", "xanh"};
        }
    }
}