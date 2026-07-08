package com.example.quanlyvitri;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietNhanVienActivity extends Activity {

    static final int REQUEST_CHON_ANH = 1001;

    NhanVienDAO dao;
    HopDongDAO hopDongDAO;          // ── MỚI
    NhanVien nhanVien;

    TextView tvAvatar, tvTen, tvMa, tvNgaySinh, tvQueQuan, tvViTri;
    ImageView imgAvatar;
    LinearLayout containerHopDong;   // ── MỚI
    TextView tvPhepNam, tvDaSuDung, tvConLai, tvNamPhep; // ── MỚI



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nhan_vien);

        dao = new NhanVienDAO(this);
        hopDongDAO = new HopDongDAO(this);  // ── MỚI
        int nhanVienId = getIntent().getIntExtra("nhan_vien_id", -1);
        nhanVien = dao.layTheoId(nhanVienId);

        tvAvatar   = findViewById(R.id.tv_avatar_lon);
        imgAvatar  = findViewById(R.id.img_avatar);
        tvTen      = findViewById(R.id.tv_ten_ct);
        tvMa       = findViewById(R.id.tv_ma_ct);
        tvNgaySinh = findViewById(R.id.tv_ngay_sinh_ct);
        tvQueQuan  = findViewById(R.id.tv_que_quan_ct);
        tvViTri    = findViewById(R.id.tv_vi_tri_ct);

        // ── MỚI: ánh xạ view hợp đồng + phép năm ──
        containerHopDong = findViewById(R.id.container_hop_dong);
        tvPhepNam  = findViewById(R.id.tv_phep_nam);
        tvDaSuDung = findViewById(R.id.tv_da_su_dung);
        tvConLai   = findViewById(R.id.tv_con_lai);
        tvNamPhep  = findViewById(R.id.tv_nam_phep);

        hienThiThongTin();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_gan_vi_tri).setOnClickListener(v -> moFormGanViTri());
        findViewById(R.id.btn_sua).setOnClickListener(v -> moFormSua());
        findViewById(R.id.btn_xoa).setOnClickListener(v -> xacNhanXoa());
        findViewById(R.id.btn_them_hop_dong).setOnClickListener(v -> moDialogHopDong(null)); // ── MỚI

        findViewById(R.id.btn_chon_anh).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CHON_ANH);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHON_ANH && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String duongDan = uri.toString();
                dao.capNhatAnh(nhanVien.getId(), duongDan);
                nhanVien = dao.layTheoId(nhanVien.getId());
                hienThiAnh(duongDan);
                Toast.makeText(this, "Đã cập nhật ảnh!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Không thể tải ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void hienThiAnh(String duongDan) {
        if (duongDan != null && !duongDan.isEmpty()) {
            try {
                Uri uri = Uri.parse(duongDan);
                InputStream stream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                imgAvatar.setImageBitmap(bitmap);
                imgAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setVisibility(View.GONE);
            } catch (Exception e) {
                imgAvatar.setVisibility(View.GONE);
                tvAvatar.setVisibility(View.VISIBLE);
            }
        } else {
            imgAvatar.setVisibility(View.GONE);
            tvAvatar.setVisibility(View.VISIBLE);
        }
    }

    void hienThiThongTin() {
        String[] parts = nhanVien.getTenNV().trim().split(" ");
        String chuDau = parts[parts.length - 1].length() > 0
                ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
        tvAvatar.setText(chuDau);
        tvTen.setText(nhanVien.getTenNV());
        tvMa.setText("Mã: " + nhanVien.getMaNV());
        tvNgaySinh.setText(nhanVien.getNgaySinh().isEmpty() ? "Chưa cập nhật" : nhanVien.getNgaySinh());
        tvQueQuan.setText(nhanVien.getQueQuan().isEmpty() ? "Chưa cập nhật" : nhanVien.getQueQuan());
        hienThiAnh(nhanVien.getDuongDanAnh());

        ViTri viTriHienTai = dao.layViTriCuaNhanVien(nhanVien.getId());
        if (viTriHienTai == null) {
            tvViTri.setText("Chưa được gán vị trí nào");
            findViewById(R.id.btn_gan_vi_tri).setVisibility(View.VISIBLE);
        } else {
            String luong = NumberFormat.getNumberInstance(Locale.US)
                    .format(viTriHienTai.getMucLuong()) + " đ/tháng";
            tvViTri.setText("• " + viTriHienTai.getTenVT()
                    + " (" + viTriHienTai.getMaVT() + ")"
                    + " — " + luong);
            findViewById(R.id.btn_gan_vi_tri).setVisibility(View.GONE);
        }

        taiDanhSachHopDong(); // ── MỚI
        taiPhepNam();          // ── MỚI
    }

    // ══════════════════════════════════════════════════
    // ══ MỚI: QUẢN LÝ HỢP ĐỒNG ══
    // ══════════════════════════════════════════════════

    void taiDanhSachHopDong() {
        containerHopDong.removeAllViews();
        List<HopDong> danhSach = hopDongDAO.layTheoNhanVien(nhanVien.getId());

        if (danhSach.isEmpty()) {
            TextView tvTrong = new TextView(this);
            tvTrong.setText("Chưa có hợp đồng nào");
            tvTrong.setTextColor(getResources().getColor(R.color.gray_text, null));
            tvTrong.setTextSize(13);
            tvTrong.setPadding(0, 0, 0, dpToPx(8));
            containerHopDong.addView(tvTrong);
            return;
        }

        for (HopDong hd : danhSach) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_hop_dong, containerHopDong, false);

            // Badge loại hợp đồng
            TextView tvLoai = card.findViewById(R.id.tv_loai_hd);
            tvLoai.setText(hd.getTenLoai());
            int badgeColor;
            switch (hd.getLoaiHopDong()) {
                case "thu_viec": badgeColor = getResources().getColor(R.color.badge_thu_viec, null); break;
                case "thoi_vu":  badgeColor = getResources().getColor(R.color.badge_thoi_vu, null); break;
                default:         badgeColor = getResources().getColor(R.color.badge_chinh_thuc, null); break;
            }
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dpToPx(4));
            badgeBg.setColor(badgeColor);
            tvLoai.setBackground(badgeBg);

            // Ngày
            ((TextView) card.findViewById(R.id.tv_ngay_bat_dau)).setText(hd.getNgayBatDau());
            String ketThuc = hd.getNgayKetThuc();
            ((TextView) card.findViewById(R.id.tv_ngay_ket_thuc))
                    .setText(ketThuc == null || ketThuc.isEmpty() ? "Không xác định" : ketThuc);

            // Trạng thái
            TextView tvTrangThai = card.findViewById(R.id.tv_trang_thai);
            String[] trangThai = hd.getTrangThai();
            tvTrangThai.setText(trangThai[0]);
            int statusColor;
            switch (trangThai[1]) {
                case "do":   statusColor = getResources().getColor(R.color.status_het_han, null); break;
                case "vang": statusColor = getResources().getColor(R.color.status_sap_het, null); break;
                default:     statusColor = getResources().getColor(R.color.status_con_han, null); break;
            }
            tvTrangThai.setTextColor(statusColor);

            // Nút Sửa với viền
            TextView btnSua = card.findViewById(R.id.btn_sua_hd);
            GradientDrawable suaBg = new GradientDrawable();
            suaBg.setCornerRadius(dpToPx(6));
            suaBg.setStroke(dpToPx(1), getResources().getColor(R.color.teal_primary, null));
            btnSua.setBackground(suaBg);
            btnSua.setOnClickListener(v -> moDialogHopDong(hd));

            // Nút Xóa với viền
            TextView btnXoa = card.findViewById(R.id.btn_xoa_hd);
            GradientDrawable xoaBg = new GradientDrawable();
            xoaBg.setCornerRadius(dpToPx(6));
            xoaBg.setStroke(dpToPx(1), getResources().getColor(R.color.red_danger, null));
            btnXoa.setBackground(xoaBg);
            btnXoa.setOnClickListener(v -> xacNhanXoaHopDong(hd));

            containerHopDong.addView(card);
        }
    }

    void moDialogHopDong(HopDong hdSua) {
        boolean isEdit = hdSua != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Sửa hợp đồng" : "Thêm hợp đồng");
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_hop_dong, null);
        builder.setView(form);

        TextView btnThuViec   = form.findViewById(R.id.btn_type_thu_viec);
        TextView btnChinhThuc = form.findViewById(R.id.btn_type_chinh_thuc);
        TextView btnThoiVu    = form.findViewById(R.id.btn_type_thoi_vu);

        // 3 ô ngày bắt đầu
        EditText edtNgayBD  = form.findViewById(R.id.edt_ngay_bd);
        EditText edtThangBD = form.findViewById(R.id.edt_thang_bd);
        EditText edtNamBD   = form.findViewById(R.id.edt_nam_bd);

        // 3 ô ngày kết thúc
        EditText edtNgayKT  = form.findViewById(R.id.edt_ngay_kt);
        EditText edtThangKT = form.findViewById(R.id.edt_thang_kt);
        EditText edtNamKT   = form.findViewById(R.id.edt_nam_kt);

        EditText edtGhiChu = form.findViewById(R.id.edt_ghi_chu_hd);

        final String[] loaiChon = {"chinh_thuc"};

        Runnable capNhatLoai = () -> {
            hienThiChonLoai(btnThuViec, "thu_viec", loaiChon[0]);
            hienThiChonLoai(btnChinhThuc, "chinh_thuc", loaiChon[0]);
            hienThiChonLoai(btnThoiVu, "thoi_vu", loaiChon[0]);
        };

        btnThuViec.setOnClickListener(v -> { loaiChon[0] = "thu_viec"; capNhatLoai.run(); });
        btnChinhThuc.setOnClickListener(v -> { loaiChon[0] = "chinh_thuc"; capNhatLoai.run(); });
        btnThoiVu.setOnClickListener(v -> { loaiChon[0] = "thoi_vu"; capNhatLoai.run(); });

        if (isEdit) {
            loaiChon[0] = hdSua.getLoaiHopDong();
            // Tách ngày bắt đầu cũ → 3 ô
            if (hdSua.getNgayBatDau() != null && hdSua.getNgayBatDau().contains("/")) {
                String[] p = hdSua.getNgayBatDau().split("/");
                if (p.length == 3) { edtNgayBD.setText(p[0]); edtThangBD.setText(p[1]); edtNamBD.setText(p[2]); }
            }
            // Tách ngày kết thúc cũ → 3 ô
            if (hdSua.getNgayKetThuc() != null && hdSua.getNgayKetThuc().contains("/")) {
                String[] p = hdSua.getNgayKetThuc().split("/");
                if (p.length == 3) { edtNgayKT.setText(p[0]); edtThangKT.setText(p[1]); edtNamKT.setText(p[2]); }
            }
            edtGhiChu.setText(hdSua.getGhiChu() != null ? hdSua.getGhiChu() : "");
        } else {
            // Mặc định ngày bắt đầu = hôm nay
            Calendar cal = Calendar.getInstance();
            edtNgayBD.setText(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
            edtThangBD.setText(String.format("%02d", cal.get(Calendar.MONTH) + 1));
            edtNamBD.setText(String.valueOf(cal.get(Calendar.YEAR)));
        }
        capNhatLoai.run();

        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // ── Validate ngày bắt đầu (bắt buộc) ──
            String sNgayBD  = edtNgayBD.getText().toString().trim();
            String sThangBD = edtThangBD.getText().toString().trim();
            String sNamBD   = edtNamBD.getText().toString().trim();

            if (sNgayBD.isEmpty() || sThangBD.isEmpty() || sNamBD.isEmpty()) {
                Toast.makeText(this, "Ngày bắt đầu không được trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!kiemTraNgayHopLe(sNgayBD, sThangBD, sNamBD)) {
                Toast.makeText(this, "Ngày bắt đầu không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ── Validate ngày kết thúc (tùy chọn) ──
            String sNgayKT  = edtNgayKT.getText().toString().trim();
            String sThangKT = edtThangKT.getText().toString().trim();
            String sNamKT   = edtNamKT.getText().toString().trim();

            boolean coNgayKT = !sNgayKT.isEmpty() || !sThangKT.isEmpty() || !sNamKT.isEmpty();
            if (coNgayKT) {
                if (sNgayKT.isEmpty() || sThangKT.isEmpty() || sNamKT.isEmpty()) {
                    Toast.makeText(this, "Ngày kết thúc chưa nhập đủ! Để trống cả 3 ô nếu không xác định.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!kiemTraNgayHopLe(sNgayKT, sThangKT, sNamKT)) {
                    Toast.makeText(this, "Ngày kết thúc không hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // ── Validate ngày kết thúc phải SAU ngày bắt đầu ──
            if (coNgayKT && !ngayKetThucSauBatDau(sNgayBD, sThangBD, sNamBD,
                    sNgayKT, sThangKT, sNamKT)) {
                Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String ngayBD = formatNgayHD(sNgayBD, sThangBD, sNamBD);
            String ngayKT = coNgayKT ? formatNgayHD(sNgayKT, sThangKT, sNamKT) : null;
            String ghiChu = edtGhiChu.getText().toString().trim();

            if (isEdit) {
                hdSua.setLoaiHopDong(loaiChon[0]);
                hdSua.setNgayBatDau(ngayBD);
                hdSua.setNgayKetThuc(ngayKT);
                hdSua.setGhiChu(ghiChu.isEmpty() ? null : ghiChu);
                hopDongDAO.sua(hdSua);
                Toast.makeText(this, "Đã cập nhật hợp đồng!", Toast.LENGTH_SHORT).show();
            } else {
                HopDong hdMoi = new HopDong(nhanVien.getId(), loaiChon[0],
                        ngayBD, ngayKT, ghiChu.isEmpty() ? null : ghiChu);
                hopDongDAO.them(hdMoi);
                Toast.makeText(this, "Đã thêm hợp đồng!", Toast.LENGTH_SHORT).show();
            }
            taiDanhSachHopDong();
            dialog.dismiss();
        });
    }

    // ── Format 3 ô → chuỗi dd/MM/yyyy ──
    String formatNgayHD(String ngay, String thang, String nam) {
        return String.format("%02d/%02d/%s",
                Integer.parseInt(ngay), Integer.parseInt(thang), nam);
    }

    // ── Kiểm tra ngày hợp lệ bằng Calendar ──
    boolean kiemTraNgayHopLe(String sNgay, String sThang, String sNam) {
        try {
            int ngay  = Integer.parseInt(sNgay);
            int thang = Integer.parseInt(sThang);
            int nam   = Integer.parseInt(sNam);

            if (thang < 1 || thang > 12) return false;
            if (nam < 1900 || nam > 2100) return false;
            if (ngay < 1) return false;

            // Dùng Calendar để biết tháng đó có bao nhiêu ngày
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, nam);
            cal.set(Calendar.MONTH, thang - 1); // Calendar.MONTH bắt đầu từ 0
            int soNgayToiDa = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            return ngay <= soNgayToiDa;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    boolean ngayKetThucSauBatDau(String ngayBD, String thangBD, String namBD,
                                 String ngayKT, String thangKT, String namKT) {
        try {
            Calendar calBD = Calendar.getInstance();
            calBD.set(Integer.parseInt(namBD), Integer.parseInt(thangBD) - 1, Integer.parseInt(ngayBD));

            Calendar calKT = Calendar.getInstance();
            calKT.set(Integer.parseInt(namKT), Integer.parseInt(thangKT) - 1, Integer.parseInt(ngayKT));

            return calKT.after(calBD);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    void hienThiChonLoai(TextView btn, String loaiCuaBtn, String loaiDangChon) {
        if (loaiCuaBtn.equals(loaiDangChon)) {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(18));
            bg.setColor(getResources().getColor(R.color.teal_primary, null));
            btn.setBackground(bg);
            btn.setTextColor(Color.WHITE);
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(18));
            bg.setStroke(dpToPx(1), getResources().getColor(R.color.gray_border, null));
            bg.setColor(Color.TRANSPARENT);
            btn.setBackground(bg);
            btn.setTextColor(getResources().getColor(R.color.text_primary, null));
        }
    }

    void xacNhanXoaHopDong(HopDong hd) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hợp đồng?")
                .setMessage("Hợp đồng " + hd.getTenLoai()
                        + " (" + hd.getNgayBatDau() + ") sẽ bị xóa.")
                .setPositiveButton("Xóa", (d, w) -> {
                    hopDongDAO.xoa(hd.getId());
                    Toast.makeText(this, "Đã xóa hợp đồng!", Toast.LENGTH_SHORT).show();
                    taiDanhSachHopDong();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ══════════════════════════════════════════════════
    // ══ MỚI: NGÀY PHÉP ══
    // ══════════════════════════════════════════════════

    void taiPhepNam() {
        int nam = Calendar.getInstance().get(Calendar.YEAR);
        int phepNam = DatabaseHelper.PHEP_NAM_MAC_DINH;
        int daSuDung = 0;

        // Đếm số ngày nghỉ phép (trạng thái = 2) trong năm hiện tại
        try {
            SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CHAM_CONG
                            + " WHERE " + DatabaseHelper.CC_NHAN_VIEN_ID + " = ?"
                            + " AND " + DatabaseHelper.CC_TRANG_THAI + " = 2"
                            + " AND " + DatabaseHelper.CC_NGAY + " LIKE ?",
                    new String[]{String.valueOf(nhanVien.getId()), "%/" + nam});
            if (cursor.moveToFirst()) daSuDung = cursor.getInt(0);
            cursor.close();
            db.close();
        } catch (Exception e) { /* bảng chưa có dữ liệu — mặc định 0 */ }

        int conLai = Math.max(0, phepNam - daSuDung);
        tvPhepNam.setText(String.valueOf(phepNam));
        tvDaSuDung.setText(String.valueOf(daSuDung));
        tvConLai.setText(String.valueOf(conLai));
        tvNamPhep.setText("Tự động tính từ chấm công năm " + nam);
    }

    int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ══════════════════════════════════════════════════
    // ══ CODE CŨ GIỮ NGUYÊN ══
    // ══════════════════════════════════════════════════

    void moFormSua() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa: " + nhanVien.getTenNV());
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_nhan_vien, null);
        builder.setView(form);

        TextView tvMaHienThi       = form.findViewById(R.id.tv_ma_nv_hien_thi);
        EditText edtTen            = form.findViewById(R.id.edt_ten_nv);
        EditText edtNgay           = form.findViewById(R.id.edt_ngay);
        EditText edtThang          = form.findViewById(R.id.edt_thang);
        EditText edtNam            = form.findViewById(R.id.edt_nam);
        EditText edtQueQuan        = form.findViewById(R.id.edt_que_quan);
        ImageView imgAvatarDialog  = form.findViewById(R.id.img_avatar_dialog);
        TextView tvAvatarDialog    = form.findViewById(R.id.tv_avatar_dialog);
        TextView btnChonAnhDialog  = form.findViewById(R.id.btn_chon_anh_dialog);
        Spinner spinnerVT          = form.findViewById(R.id.spinner_vi_tri_dialog);

        String ngaySinhCu = nhanVien.getNgaySinh();
        if (ngaySinhCu != null && ngaySinhCu.contains("/")) {
            String[] p = ngaySinhCu.split("/");
            if (p.length == 3) { edtNgay.setText(p[0]); edtThang.setText(p[1]); edtNam.setText(p[2]); }
        }

        tvMaHienThi.setText("Mã: " + nhanVien.getMaNV());
        edtTen.setText(nhanVien.getTenNV());
        edtQueQuan.setText(nhanVien.getQueQuan());

        String anhHienTai = nhanVien.getDuongDanAnh();
        if (anhHienTai != null && !anhHienTai.isEmpty()) {
            try {
                Uri uri = Uri.parse(anhHienTai);
                InputStream stream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                imgAvatarDialog.setImageBitmap(bitmap);
                imgAvatarDialog.setVisibility(View.VISIBLE);
                tvAvatarDialog.setVisibility(View.GONE);
            } catch (Exception e) {
                String[] p = nhanVien.getTenNV().trim().split(" ");
                tvAvatarDialog.setText(p[p.length-1].length() > 0
                        ? String.valueOf(p[p.length-1].charAt(0)).toUpperCase() : "?");
            }
        } else {
            String[] p = nhanVien.getTenNV().trim().split(" ");
            tvAvatarDialog.setText(p[p.length-1].length() > 0
                    ? String.valueOf(p[p.length-1].charAt(0)).toUpperCase() : "?");
        }

        btnChonAnhDialog.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CHON_ANH);
        });

        ViTriDAO viTriDAO = new ViTriDAO(this);
        List<ViTri> dsViTri = viTriDAO.layTatCa();
        List<String> tenViTri = new ArrayList<>();
        tenViTri.add("-- Chưa chọn vị trí --");
        for (ViTri vt : dsViTri) tenViTri.add(vt.getTenVT() + " (" + vt.getMaVT() + ")");
        android.widget.ArrayAdapter<String> spAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tenViTri);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVT.setAdapter(spAdapter);

        ViTri viTriHienTai = dao.layViTriCuaNhanVien(nhanVien.getId());
        if (viTriHienTai != null) {
            for (int i = 0; i < dsViTri.size(); i++) {
                if (dsViTri.get(i).getId() == viTriHienTai.getId()) {
                    spinnerVT.setSelection(i + 1); break;
                }
            }
        }

        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            if (ten.isEmpty()) {
                Toast.makeText(this, "Tên không được trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            int viTriPos = spinnerVT.getSelectedItemPosition();
            boolean chonViTriMoi = viTriPos > 0;
            boolean chonKhacViTriCu = viTriHienTai != null && chonViTriMoi
                    && dsViTri.get(viTriPos - 1).getId() != viTriHienTai.getId();
            if (chonKhacViTriCu) {
                ViTri vtMoi = dsViTri.get(viTriPos - 1);
                new AlertDialog.Builder(this)
                        .setTitle("Thay thế vị trí?")
                        .setMessage("Nhân viên đang ở \"" + viTriHienTai.getTenVT()
                                + "\".\nChuyển sang \"" + vtMoi.getTenVT() + "\"?")
                        .setPositiveButton("Xác nhận", (d2, w2) -> {
                            luuNhanVien(edtTen, edtNgay, edtThang, edtNam, edtQueQuan, viTriPos, dsViTri);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Hủy", null).show();
            } else {
                luuNhanVien(edtTen, edtNgay, edtThang, edtNam, edtQueQuan, viTriPos, dsViTri);
                dialog.dismiss();
            }
        });
    }

    void moFormGanViTri() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gán vị trí cho: " + nhanVien.getTenNV());
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_gan_vitri, null);
        builder.setView(form);

        TextView tvTen2    = form.findViewById(R.id.tv_ten_nv_gan);
        TextView tvMaGan   = form.findViewById(R.id.tv_ma_nv_gan);
        Spinner spinner    = form.findViewById(R.id.spinner_chon_vitri);
        ListView lvHienTai = form.findViewById(R.id.lv_vitri_hien_tai);
        TextView tvChuaCo  = form.findViewById(R.id.tv_chua_co_vitri);

        tvTen2.setText(nhanVien.getTenNV());
        tvMaGan.setText("Mã: " + nhanVien.getMaNV());

        ViTriDAO viTriDAO = new ViTriDAO(this);
        List<ViTri> dsViTri = viTriDAO.layTatCa();
        android.widget.ArrayAdapter<ViTri> spinnerAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dsViTri);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        ViTri viTriHienTai = dao.layViTriCuaNhanVien(nhanVien.getId());
        if (viTriHienTai == null) {
            lvHienTai.setVisibility(View.GONE);
            tvChuaCo.setVisibility(View.VISIBLE);
        } else {
            lvHienTai.setVisibility(View.VISIBLE);
            tvChuaCo.setVisibility(View.GONE);
            List<ViTri> dsHienTai = new ArrayList<>();
            dsHienTai.add(viTriHienTai);
            GanViTriAdapter ganAdapter = new GanViTriAdapter(this, dsHienTai, viTri -> {
                dao.capNhatViTri(nhanVien.getId(), -1);
                Toast.makeText(this, "Đã hủy gán!", Toast.LENGTH_SHORT).show();
                hienThiThongTin();
            });
            lvHienTai.setAdapter(ganAdapter);
        }

        builder.setPositiveButton("Gán", (dialog, which) -> {
            if (dsViTri.isEmpty()) {
                Toast.makeText(this, "Chưa có vị trí nào!", Toast.LENGTH_SHORT).show();
                return;
            }
            ViTri viTriChon = dsViTri.get(spinner.getSelectedItemPosition());
            dao.capNhatViTri(nhanVien.getId(), viTriChon.getId());
            Toast.makeText(this, "Gán thành công!", Toast.LENGTH_SHORT).show();
            hienThiThongTin();
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    void xacNhanXoa() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa nhân viên " + nhanVien.getTenNV() + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    hopDongDAO.xoaTheoNhanVien(nhanVien.getId()); // ── MỚI: xóa hợp đồng trước
                    new ChamCongDAO(this).xoaTheoNhanVien(nhanVien.getId()); // ← THÊM DÒNG NÀY
                    new LuongDAO(this).xoaTheoNhanVien(nhanVien.getId()); // ← THÊM
                    dao.xoa(nhanVien.getId());
                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    void luuNhanVien(EditText edtTen, EditText edtNgay, EditText edtThang,
                     EditText edtNam, EditText edtQueQuan,
                     int viTriPos, List<ViTri> dsViTri) {
        int result = dao.sua(nhanVien.getId(), nhanVien.getMaNV(),
                edtTen.getText().toString().trim(),
                formatNgaySinh(edtNgay, edtThang, edtNam),
                edtQueQuan.getText().toString().trim());
        if (result > 0) {
            int viTriMoiId = viTriPos > 0 ? dsViTri.get(viTriPos - 1).getId() : -1;
            dao.capNhatViTri(nhanVien.getId(), viTriMoiId);
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            nhanVien = dao.layTheoId(nhanVien.getId());
            hienThiThongTin();
        }
    }

    String formatNgaySinh(EditText edtNgay, EditText edtThang, EditText edtNam) {
        String ngay  = edtNgay.getText().toString().trim();
        String thang = edtThang.getText().toString().trim();
        String nam   = edtNam.getText().toString().trim();
        if (ngay.isEmpty() && thang.isEmpty() && nam.isEmpty()) return "";
        if (!ngay.isEmpty())  ngay  = String.format("%02d", Integer.parseInt(ngay));
        if (!thang.isEmpty()) thang = String.format("%02d", Integer.parseInt(thang));
        return ngay + "/" + thang + "/" + nam;
    }
}