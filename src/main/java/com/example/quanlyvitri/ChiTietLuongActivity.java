package com.example.quanlyvitri;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ChiTietLuongActivity extends Activity {

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_luong);

        int nvId  = getIntent().getIntExtra("nhan_vien_id", -1);
        int thang = getIntent().getIntExtra("thang", 1);
        int nam   = getIntent().getIntExtra("nam", 2026);

        findViewById(R.id.btn_back_luong).setOnClickListener(v -> finish());

        // Lấy dữ liệu lương
        LuongDAO luongDAO = new LuongDAO(this);
        List<BangLuong> ds = luongDAO.layDanhSach(thang, nam);
        BangLuong bl = null;
        for (BangLuong item : ds) {
            if (item.getNhanVienId() == nvId) { bl = item; break; }
        }
        if (bl == null) { finish(); return; }

        // Avatar
        String[] parts = bl.getTenNV().trim().split(" ");
        String chuDau = parts[parts.length - 1].length() > 0
                ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
        ((TextView) findViewById(R.id.tv_avatar_ct_luong)).setText(chuDau);

        // Thông tin NV
        ((TextView) findViewById(R.id.tv_ten_ct_luong)).setText(bl.getTenNV());
        ((TextView) findViewById(R.id.tv_vitri_ct_luong))
                .setText(bl.getTenViTri() + " · " + bl.getMaNV());
        ((TextView) findViewById(R.id.tv_thang_ct_luong))
                .setText(String.format("Tháng\n%02d/%d", thang, nam));

        // Chấm công
        ((TextView) findViewById(R.id.tv_ct_co_mat)).setText(String.valueOf(bl.getCoMat()));
        ((TextView) findViewById(R.id.tv_ct_nghi_phep)).setText(String.valueOf(bl.getNghiPhep()));
        ((TextView) findViewById(R.id.tv_ct_vang)).setText(String.valueOf(bl.getVang()));

        // Phép năm
        ((TextView) findViewById(R.id.tv_ct_phep_da_dung)).setText(String.valueOf(bl.getPhepDaDungNam()));
        ((TextView) findViewById(R.id.tv_ct_phep_con_lai)).setText(String.valueOf(bl.getPhepConLai()));

        // Lương chi tiết
        ((TextView) findViewById(R.id.tv_row_luong_vt)).setText(fmt(bl.getLuongCoBan()));
        ((TextView) findViewById(R.id.tv_row_ngay_chuan)).setText(String.valueOf(bl.getNgayChuan()));
        ((TextView) findViewById(R.id.tv_row_ngay_cong)).setText(String.valueOf(bl.getNgayCongThuc()));
        ((TextView) findViewById(R.id.tv_row_he_so)).setText(bl.getHeSoCong());
        ((TextView) findViewById(R.id.tv_row_thuong)).setText(fmt(bl.getThuongChuyenCan()));
        // Tính và hiển thị tăng ca (nếu có)
        double luongCoBan = bl.getNgayChuan() > 0
                ? (double) bl.getNgayCongThuc() / bl.getNgayChuan() * bl.getLuongCoBan() : 0;
        double luongTangCa = bl.getLuongThucNhan() - Math.round(luongCoBan) - bl.getThuongChuyenCan();

        if (luongTangCa > 0) {
            // Có tăng ca — hiện các dòng
            // Tính ngược: tăng ca = thực nhận - lương cơ bản - thưởng
            double luongMotNgay = bl.getNgayChuan() > 0 ? bl.getLuongCoBan() / bl.getNgayChuan() : 0;

            // Đếm ngày tăng ca từ chấm công
            ChamCongDAO ccDAO = new ChamCongDAO(this);
            NgayNghiLeDAO nlDAO = new NgayNghiLeDAO(this);
            String pattern = String.format("%%/%02d/%d", thang, nam);
            android.database.Cursor cursor = new DatabaseHelper(this).getReadableDatabase().rawQuery(
                    "SELECT " + DatabaseHelper.CC_NGAY + ", " + DatabaseHelper.CC_TRANG_THAI
                            + " FROM " + DatabaseHelper.TABLE_CHAM_CONG
                            + " WHERE " + DatabaseHelper.CC_NHAN_VIEN_ID + " = ?"
                            + " AND " + DatabaseHelper.CC_TRANG_THAI + " = 1"
                            + " AND " + DatabaseHelper.CC_NGAY + " LIKE ?",
                    new String[]{String.valueOf(nvId), pattern});

            int tcT7CN = 0, tcLe = 0;
            while (cursor.moveToNext()) {
                String ngay = cursor.getString(0);
                if (nlDAO.laNgayLe(ngay)) tcLe++;
                else {
                    try {
                        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("dd/MM/yyyy",
                                java.util.Locale.getDefault());
                        java.util.Calendar cal2 = java.util.Calendar.getInstance();
                        cal2.setTime(sdf2.parse(ngay));
                        int dow = cal2.get(java.util.Calendar.DAY_OF_WEEK);
                        if (dow == java.util.Calendar.SATURDAY || dow == java.util.Calendar.SUNDAY) tcT7CN++;
                    } catch (Exception e) {}
                }
            }
            cursor.close();

            if (tcT7CN > 0) {
                findViewById(R.id.row_tang_ca_t7cn).setVisibility(android.view.View.VISIBLE);
                ((TextView) findViewById(R.id.tv_row_tang_ca_t7cn))
                        .setText(tcT7CN + " ngày = " + fmt(tcT7CN * luongMotNgay * 2));
            }
            if (tcLe > 0) {
                findViewById(R.id.row_tang_ca_le).setVisibility(android.view.View.VISIBLE);
                ((TextView) findViewById(R.id.tv_row_tang_ca_le))
                        .setText(tcLe + " ngày = " + fmt(tcLe * luongMotNgay * 3));
            }
        }

        // Lương thực nhận
        ((TextView) findViewById(R.id.tv_ct_thuc_nhan)).setText(fmt(bl.getLuongThucNhan()));
    }

    String fmt(double amount) {
        return nf.format((long) amount) + " đ";
    }
}