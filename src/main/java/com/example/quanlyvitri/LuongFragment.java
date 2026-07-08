package com.example.quanlyvitri;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LuongFragment extends Fragment {

    LuongDAO luongDAO;
    int thangHienTai, namHienTai;

    TextView tvKyLuong, tvThang, tvNam;
    TextView tvTongNV, tvNgayChuan, tvNVChuyenCan, tvTongQuy;
    LinearLayout containerDS;

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_luong, container, false);

        luongDAO = new LuongDAO(getActivity());
        Calendar cal = Calendar.getInstance();
        thangHienTai = cal.get(Calendar.MONTH) + 1;
        namHienTai = cal.get(Calendar.YEAR);

        tvKyLuong = view.findViewById(R.id.tv_ky_luong);
        tvThang   = view.findViewById(R.id.tv_thang);
        tvNam     = view.findViewById(R.id.tv_nam);
        tvTongNV      = view.findViewById(R.id.tv_tong_nv);
        tvNgayChuan   = view.findViewById(R.id.tv_ngay_chuan);
        tvNVChuyenCan = view.findViewById(R.id.tv_nv_chuyen_can);
        tvTongQuy     = view.findViewById(R.id.tv_tong_quy);
        containerDS   = view.findViewById(R.id.container_ds_luong);

        view.findViewById(R.id.btn_thang_truoc).setOnClickListener(v -> doiThang(-1));
        view.findViewById(R.id.btn_thang_sau).setOnClickListener(v -> doiThang(1));
        view.findViewById(R.id.btn_tinh_luong).setOnClickListener(v -> tinhLuong());

        taiDuLieu();
        return view;
    }

    void taiDuLieu() {
        tvKyLuong.setText("Kỳ lương tháng " + String.format("%02d/%d", thangHienTai, namHienTai));
        tvThang.setText(String.format("%02d", thangHienTai));
        tvNam.setText(String.valueOf(namHienTai));

        List<BangLuong> danhSach = luongDAO.layDanhSach(thangHienTai, namHienTai);

        // Thống kê
        int ngayChuan = luongDAO.ngayChuanTrongThang(thangHienTai, namHienTai);
        int nvChuyenCan = 0;
        double tongQuy = 0;
        for (BangLuong bl : danhSach) {
            tongQuy += bl.getLuongThucNhan();
            if (bl.getThuongChuyenCan() > 0) nvChuyenCan++;
        }

        tvTongNV.setText(String.valueOf(danhSach.size()));
        tvNgayChuan.setText(String.valueOf(ngayChuan));
        tvNVChuyenCan.setText(String.valueOf(nvChuyenCan));
        tvTongQuy.setText(nf.format((long) tongQuy) + " đ");

        // Danh sách
        containerDS.removeAllViews();
        if (danhSach.isEmpty()) {
            TextView tv = new TextView(getActivity());
            tv.setText("Nhấn TÍNH LƯƠNG để bắt đầu");
            tv.setTextColor(getActivity().getResources().getColor(R.color.gray_text, null));
            tv.setTextSize(14);
            tv.setPadding(0, 24, 0, 24);
            tv.setGravity(android.view.Gravity.CENTER);
            containerDS.addView(tv);
            return;
        }

        for (BangLuong bl : danhSach) {
            View card = LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_luong, containerDS, false);

            // Avatar
            String[] parts = bl.getTenNV().trim().split(" ");
            String chuDau = parts[parts.length - 1].length() > 0
                    ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
            ((TextView) card.findViewById(R.id.tv_avatar_luong)).setText(chuDau);

            ((TextView) card.findViewById(R.id.tv_ten_luong)).setText(bl.getTenNV());
            ((TextView) card.findViewById(R.id.tv_vi_tri_luong))
                    .setText(bl.getTenViTri() + " · " + bl.getMaNV());
            ((TextView) card.findViewById(R.id.tv_thuc_nhan))
                    .setText(nf.format((long) bl.getLuongThucNhan()) + " đ");

            ((TextView) card.findViewById(R.id.tv_cong_thuc))
                    .setText(String.valueOf(bl.getNgayCongThuc()));
            ((TextView) card.findViewById(R.id.tv_phep_da_dung))
                    .setText(String.valueOf(bl.getPhepDaDungNam()));
            ((TextView) card.findViewById(R.id.tv_phep_con_lai))
                    .setText(String.valueOf(bl.getPhepConLai()));

            ((TextView) card.findViewById(R.id.tv_luong_vt_thuong))
                    .setText("Lương VT " + nf.format((long) bl.getLuongCoBan()) + " đ"
                            + " · Thưởng " + nf.format((long) bl.getThuongChuyenCan()));

            // Nút Chi tiết → mở Activity
            card.findViewById(R.id.btn_chi_tiet).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChiTietLuongActivity.class);
                intent.putExtra("nhan_vien_id", bl.getNhanVienId());
                intent.putExtra("thang", thangHienTai);
                intent.putExtra("nam", namHienTai);
                startActivity(intent);
            });

            containerDS.addView(card);
        }
    }

    void tinhLuong() {
        luongDAO.tinhLuongThang(thangHienTai, namHienTai);
        taiDuLieu();
        Toast.makeText(getActivity(),
                "Đã tính lương tháng " + String.format("%02d/%d", thangHienTai, namHienTai),
                Toast.LENGTH_SHORT).show();
    }

    void doiThang(int delta) {
        thangHienTai += delta;
        if (thangHienTai > 12) { thangHienTai = 1; namHienTai++; }
        if (thangHienTai < 1)  { thangHienTai = 12; namHienTai--; }
        taiDuLieu();
    }
}