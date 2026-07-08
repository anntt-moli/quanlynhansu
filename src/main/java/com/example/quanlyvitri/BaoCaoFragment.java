package com.example.quanlyvitri;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BaoCaoFragment extends Fragment {

    // Tabs
    TextView tabTongQuan, tabNvTheoViTri, tabLuongCao;
    ScrollView contentTongQuan;
    LinearLayout contentNvTheoViTri, contentLuongCao;

    // Tab 0: Tổng quan
    TextView tvNgayBaoCao, tvTongNV, tvTongVT, tvChuaGan, tvVTTrong;
    TextView tvBcCoMat, tvBcNghiPhep, tvBcVang;
    LinearLayout containerPhanBo, containerTuoi, containerSinhNhat;
    // Tab 0 bổ sung
    TextView tvTitleSinhNhat;
    LinearLayout containerVangPhep, containerBieuDoTuan, containerBieuDoHomNay;
    int thangSinhNhat;

    // Tab 1: Theo vị trí (giữ nguyên)
    int viTriDangChon = 0;
    Spinner spinnerViTri;
    ListView lvNvTheoViTri;
    TextView tvSoNvTheoViTri, tvKhongCoNv;

    // Tab 2: Lương > 10tr
    ListView lvLuongCao;
    TextView tvKhongCoLuongCao, tvTieuDeLuong, tvLcThang;
    int lcThang, lcNam; // tháng/năm đang xem

    // DAO
    ViTriDAO viTriDAO;
    NhanVienDAO nhanVienDAO;

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bao_cao, container, false);

        viTriDAO = new ViTriDAO(getActivity());
        nhanVienDAO = new NhanVienDAO(getActivity());

        // Tabs
        tabTongQuan    = view.findViewById(R.id.tab_tong_quan);
        tabNvTheoViTri = view.findViewById(R.id.tab_nv_theo_vitri);
        tabLuongCao    = view.findViewById(R.id.tab_luong_cao);
        contentTongQuan    = view.findViewById(R.id.content_tong_quan);
        contentNvTheoViTri = view.findViewById(R.id.content_nv_theo_vitri);
        contentLuongCao    = view.findViewById(R.id.content_luong_cao);

        // Tab 0
        tvNgayBaoCao = view.findViewById(R.id.tv_ngay_bao_cao);
        tvTongNV  = view.findViewById(R.id.tv_tong_nv);
        tvTongVT  = view.findViewById(R.id.tv_tong_vt);
        tvChuaGan = view.findViewById(R.id.tv_chua_gan);
        tvVTTrong = view.findViewById(R.id.tv_vt_trong);

        containerPhanBo   = view.findViewById(R.id.container_phan_bo);
        containerTuoi     = view.findViewById(R.id.container_tuoi);
        containerSinhNhat = view.findViewById(R.id.container_sinh_nhat);

        tvTitleSinhNhat   = view.findViewById(R.id.tv_title_sinh_nhat);
        containerVangPhep = view.findViewById(R.id.container_vang_phep);
        containerBieuDoTuan = view.findViewById(R.id.container_bieu_do_tuan);
        containerBieuDoHomNay = view.findViewById(R.id.container_bieu_do_hom_nay);

        thangSinhNhat = Calendar.getInstance().get(Calendar.MONTH) + 1;
        view.findViewById(R.id.btn_sn_thang_truoc).setOnClickListener(v -> {
            thangSinhNhat--; if (thangSinhNhat < 1) thangSinhNhat = 12;
            taiSinhNhat();
        });
        view.findViewById(R.id.btn_sn_thang_sau).setOnClickListener(v -> {
            thangSinhNhat++; if (thangSinhNhat > 12) thangSinhNhat = 1;
            taiSinhNhat();
        });

        // Tab 1
        spinnerViTri    = view.findViewById(R.id.spinner_vi_tri);
        lvNvTheoViTri   = view.findViewById(R.id.lv_nv_theo_vitri);
        tvSoNvTheoViTri = view.findViewById(R.id.tv_so_nv_theo_vitri);
        tvKhongCoNv     = view.findViewById(R.id.tv_khong_co_nv);

        // Tab 2
        // Tab 2
        lvLuongCao        = view.findViewById(R.id.lv_luong_cao);
        tvKhongCoLuongCao = view.findViewById(R.id.tv_khong_co_luong_cao);
        tvTieuDeLuong     = view.findViewById(R.id.tv_tieu_de_luong);
        tvLcThang         = view.findViewById(R.id.tv_lc_thang);

        Calendar calLC = Calendar.getInstance();
        lcThang = calLC.get(Calendar.MONTH) + 1;
        lcNam   = calLC.get(Calendar.YEAR);

        view.findViewById(R.id.btn_lc_thang_truoc).setOnClickListener(v -> {
            lcThang--;
            if (lcThang < 1) { lcThang = 12; lcNam--; }
            taiTab2();
        });
        view.findViewById(R.id.btn_lc_thang_sau).setOnClickListener(v -> {
            lcThang++;
            if (lcThang > 12) { lcThang = 1; lcNam++; }
            taiTab2();
        });

        tabTongQuan.setOnClickListener(v -> chonTab(0));
        tabNvTheoViTri.setOnClickListener(v -> chonTab(1));
        tabLuongCao.setOnClickListener(v -> chonTab(2));

        // Ngày hiện tại trên header
        String[] thuVN = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư",
                "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        Calendar cal = Calendar.getInstance();
        String ngay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
        tvNgayBaoCao.setText(thuVN[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", " + ngay);

        chonTab(0);
        taiTongQuan();
        taiTab1();
        taiTab2();

        return view;
    }

    // ══════════════════════════════════════
    // ══ CHUYỂN TAB ══
    // ══════════════════════════════════════

    void chonTab(int index) {
        int teal = getResources().getColor(R.color.teal_primary, null);
        int gray = getResources().getColor(R.color.gray_text, null);
        int tealLight = getResources().getColor(R.color.teal_light, null);
        int white = getResources().getColor(R.color.white, null);

        tabTongQuan.setTextColor(gray); tabTongQuan.setBackgroundColor(white);
        tabNvTheoViTri.setTextColor(gray); tabNvTheoViTri.setBackgroundColor(white);
        tabLuongCao.setTextColor(gray); tabLuongCao.setBackgroundColor(white);
        contentTongQuan.setVisibility(View.GONE);
        contentNvTheoViTri.setVisibility(View.GONE);
        contentLuongCao.setVisibility(View.GONE);

        switch (index) {
            case 0:
                tabTongQuan.setTextColor(teal);
                tabTongQuan.setBackgroundColor(tealLight);
                contentTongQuan.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabNvTheoViTri.setTextColor(teal);
                tabNvTheoViTri.setBackgroundColor(tealLight);
                contentNvTheoViTri.setVisibility(View.VISIBLE);
                break;
            case 2:
                tabLuongCao.setTextColor(teal);
                tabLuongCao.setBackgroundColor(tealLight);
                contentLuongCao.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ══════════════════════════════════════
    // ══ TAB 0: TỔNG QUAN ══
    // ══════════════════════════════════════

    public void taiTongQuan() {
        List<NhanVien> dsNV = nhanVienDAO.layTatCa();
        List<ViTri> dsVT = viTriDAO.layTatCa();

        // 4 thẻ thống kê
        tvTongNV.setText(String.valueOf(dsNV.size()));
        tvTongVT.setText(String.valueOf(dsVT.size()));
        tvChuaGan.setText(String.valueOf(nhanVienDAO.layNhanVienChuaGan().size()));
        int vtTrong = 0;
        for (ViTri vt : dsVT)
            if (nhanVienDAO.layTheoViTri(vt.getId()).isEmpty()) vtTrong++;
        tvVTTrong.setText(String.valueOf(vtTrong));

        // ── Chấm công hôm nay — biểu đồ cột ──
        String homNay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        ChamCongDAO ccDAO = new ChamCongDAO(getActivity());
        Map<Integer, Integer> ccMap = ccDAO.layMapTheoNgay(homNay);
        int cm = 0, np = 0, vg = 0;
        List<String> dsVang = new ArrayList<>(), dsPhep = new ArrayList<>();
        for (NhanVien nv : dsNV) {
            Integer tt = ccMap.get(nv.getId());
            if (tt != null) {
                if (tt == 1 || tt == 4) cm++;
                else if (tt == 3) { vg++; dsVang.add(nv.getTenNV()); }
                else if (tt == 2) { np++; dsPhep.add(nv.getTenNV()); }
            }
        }

        // Biểu đồ hôm nay: Số lớn + thanh tỷ lệ + legend
        containerBieuDoHomNay.removeAllViews();
        int tongNV = dsNV.size();

        // Cột trái: số lớn + thanh ngang
        LinearLayout colTrai = new LinearLayout(getActivity());
        colTrai.setOrientation(LinearLayout.VERTICAL);
        colTrai.setGravity(android.view.Gravity.CENTER);
        colTrai.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Số lớn + phân số CÙNG HÀNG
        LinearLayout rowSo = new LinearLayout(getActivity());
        rowSo.setOrientation(LinearLayout.HORIZONTAL);
        rowSo.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

        TextView tvSoLon = new TextView(getActivity());
        tvSoLon.setText(String.valueOf(cm));
        tvSoLon.setTextSize(36);
        tvSoLon.setTypeface(null, Typeface.BOLD);
        tvSoLon.setTextColor(getResources().getColor(R.color.teal_primary, null));

        TextView tvPhanSo = new TextView(getActivity());
        tvPhanSo.setText("/" + tongNV);
        tvPhanSo.setTextSize(14);
        tvPhanSo.setTextColor(getResources().getColor(R.color.gray_text, null));
        tvPhanSo.setPadding(0, 0, 0, dp(6)); // căn đáy cho khớp với số lớn

        rowSo.addView(tvSoLon);
        rowSo.addView(tvPhanSo);
        colTrai.addView(rowSo);

        // Thanh ngang tỷ lệ
        LinearLayout thanh = new LinearLayout(getActivity());
        thanh.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams thanhLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        thanhLP.setMargins(dp(16), dp(6), dp(16), 0);
        thanh.setLayoutParams(thanhLP);

        if (tongNV > 0) {
            View barXanh = new View(getActivity());
            barXanh.setBackgroundColor(getResources().getColor(R.color.status_con_han, null));
            barXanh.setLayoutParams(new LinearLayout.LayoutParams(0, dp(8), cm));
            thanh.addView(barXanh);

            if (np > 0) {
                View barXanhDuong = new View(getActivity());
                barXanhDuong.setBackgroundColor(getResources().getColor(R.color.badge_thoi_vu, null));
                barXanhDuong.setLayoutParams(new LinearLayout.LayoutParams(0, dp(8), np));
                thanh.addView(barXanhDuong);
            }
            if (vg > 0) {
                View barDo = new View(getActivity());
                barDo.setBackgroundColor(getResources().getColor(R.color.status_het_han, null));
                barDo.setLayoutParams(new LinearLayout.LayoutParams(0, dp(8), vg));
                thanh.addView(barDo);
            }

            int chuaCham = tongNV - cm - np - vg;
            if (chuaCham > 0) {
                View barXam = new View(getActivity());
                barXam.setBackgroundColor(getResources().getColor(R.color.gray_border, null));
                barXam.setLayoutParams(new LinearLayout.LayoutParams(0, dp(8), chuaCham));
                thanh.addView(barXam);
            }
        }
        colTrai.addView(thanh);

        // Cột phải: legend
        LinearLayout colPhai = new LinearLayout(getActivity());
        colPhai.setOrientation(LinearLayout.VERTICAL);
        colPhai.setGravity(android.view.Gravity.CENTER_VERTICAL);
        colPhai.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvCM = new TextView(getActivity());
        tvCM.setText("● Có mặt:  " + cm);
        tvCM.setTextSize(12);
        tvCM.setTextColor(getResources().getColor(R.color.status_con_han, null));
        tvCM.setPadding(0, dp(2), 0, dp(2));

        TextView tvVG = new TextView(getActivity());
        tvVG.setText("● Vắng:  " + vg);
        tvVG.setTextSize(12);
        tvVG.setTextColor(getResources().getColor(R.color.status_het_han, null));
        tvVG.setPadding(0, dp(2), 0, dp(2));

        TextView tvNP = new TextView(getActivity());
        tvNP.setText("● Nghỉ phép:  " + np);
        tvNP.setTextSize(12);
        tvNP.setTextColor(getResources().getColor(R.color.badge_thoi_vu, null));
        tvNP.setPadding(0, dp(2), 0, dp(2));

        colPhai.addView(tvCM);
        colPhai.addView(tvVG);
        colPhai.addView(tvNP);

        containerBieuDoHomNay.addView(colTrai);
        containerBieuDoHomNay.addView(colPhai);

        // Danh sách vắng/nghỉ phép
        containerVangPhep.removeAllViews();
        if (!dsVang.isEmpty()) {
            TextView tv = new TextView(getActivity());
            tv.setText("Vắng: " + String.join(", ", dsVang));
            tv.setTextSize(12);
            tv.setTextColor(getResources().getColor(R.color.status_het_han, null));
            tv.setPadding(0, dp(4), 0, dp(2));
            containerVangPhep.addView(tv);
        }
        if (!dsPhep.isEmpty()) {
            TextView tv = new TextView(getActivity());
            tv.setText("Nghỉ phép: " + String.join(", ", dsPhep));
            tv.setTextSize(12);
            tv.setTextColor(getResources().getColor(R.color.badge_thoi_vu, null));
            tv.setPadding(0, dp(2), 0, dp(4));
            containerVangPhep.addView(tv);
        }

        // ── Biểu đồ tuần T2→CN (7 ngày) ──
        containerBieuDoTuan.removeAllViews();
        String[] tenThu = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        Calendar calTuan = Calendar.getInstance();
        while (calTuan.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            calTuan.add(Calendar.DAY_OF_MONTH, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        int maxCC = Math.max(dsNV.size(), 1);
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            String ngay = sdf.format(calTuan.getTime());
            Map<Integer, Integer> map = ccDAO.layMapTheoNgay(ngay);
            int soCoMat = 0;
            for (int tt : map.values()) if (tt == 1 || tt == 4) soCoMat++;
            boolean isToday = sdf.format(today.getTime()).equals(ngay);
            boolean isFuture = calTuan.after(today);

            LinearLayout col = new LinearLayout(getActivity());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvSo = new TextView(getActivity());
            tvSo.setText(isFuture ? "–" : String.valueOf(soCoMat));
            tvSo.setTextSize(11);
            tvSo.setGravity(android.view.Gravity.CENTER);
            tvSo.setTextColor(getResources().getColor(
                    isToday ? R.color.teal_primary : R.color.gray_text, null));
            col.addView(tvSo);

            View bar = new View(getActivity());
            int barH = isFuture ? dp(4) : dp(4 + (soCoMat * 40 / maxCC));
            LinearLayout.LayoutParams barLP = new LinearLayout.LayoutParams(dp(20), barH);
            barLP.setMargins(0, dp(4), 0, dp(4));
            bar.setLayoutParams(barLP);
            bar.setBackgroundColor(getResources().getColor(
                    isToday ? R.color.teal_primary :
                            isFuture ? R.color.gray_border : R.color.status_con_han, null));
            col.addView(bar);

            TextView tvThu = new TextView(getActivity());
            tvThu.setText(tenThu[i]);
            tvThu.setTextSize(11);
            tvThu.setGravity(android.view.Gravity.CENTER);
            tvThu.setTypeface(null, isToday ? Typeface.BOLD : Typeface.NORMAL);
            tvThu.setTextColor(getResources().getColor(
                    isToday ? R.color.teal_primary : R.color.gray_text, null));
            col.addView(tvThu);

            containerBieuDoTuan.addView(col);
            calTuan.add(Calendar.DAY_OF_MONTH, 1);
        }

        // ── Phân bố vị trí + badge + click chuyển tab ──
        containerPhanBo.removeAllViews();
        for (int idx = 0; idx < dsVT.size(); idx++) {
            ViTri vt = dsVT.get(idx);
            int soNV = nhanVienDAO.layTheoViTri(vt.getId()).size();
            final int spinnerIdx = idx + 1;

            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(8), 0, dp(8));
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView dot = new TextView(getActivity());
            dot.setText("● "); dot.setTextSize(14);
            dot.setTextColor(getResources().getColor(R.color.teal_primary, null));

            TextView tvTen = new TextView(getActivity());
            tvTen.setText(vt.getTenVT()); tvTen.setTextSize(13);
            tvTen.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvTen.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            int canNV = vt.getSoNhanLuc();
            TextView tvSoNguoi = new TextView(getActivity());
            tvSoNguoi.setText(soNV + "/" + canNV); tvSoNguoi.setTextSize(13);
            tvSoNguoi.setTypeface(null, Typeface.BOLD);
            tvSoNguoi.setPadding(dp(8), 0, dp(8), 0);

            TextView badge = new TextView(getActivity());
            badge.setTextSize(11); badge.setTypeface(null, Typeface.BOLD);
            if (soNV >= canNV) {
                badge.setText("Đủ");
                badge.setTextColor(getResources().getColor(R.color.status_con_han, null));
            } else {
                badge.setText("Thiếu " + (canNV - soNV));
                badge.setTextColor(getResources().getColor(R.color.status_het_han, null));
            }

            TextView arrow = new TextView(getActivity());
            arrow.setText(" ›"); arrow.setTextSize(16);
            arrow.setTextColor(getResources().getColor(R.color.gray_text, null));

            row.addView(dot); row.addView(tvTen); row.addView(tvSoNguoi);
            row.addView(badge); row.addView(arrow);
            row.setClickable(true); row.setFocusable(true);
            row.setOnClickListener(v -> { chonTab(1); spinnerViTri.setSelection(spinnerIdx); });
            containerPhanBo.addView(row);
        }

        // ── Thống kê tuổi (dots) ──
        containerTuoi.removeAllViews();
        int t18 = 0, t26 = 0, t36 = 0, t46 = 0;
        for (NhanVien nv : dsNV) {
            int tuoi = tinhTuoi(nv.getNgaySinh());
            if (tuoi <= 0) continue;
            if (tuoi <= 25) t18++;
            else if (tuoi <= 35) t26++;
            else if (tuoi <= 45) t36++;
            else t46++;
        }
        themDongTuoi("18–25 tuổi", t18);
        themDongTuoi("26–35 tuổi", t26);
        themDongTuoi("36–45 tuổi", t36);
        themDongTuoi("Trên 45 tuổi", t46);

        // Sinh nhật
        taiSinhNhat();
    }

    // ── Helper: dòng tuổi với dots trực quan ──
    void themDongTuoi(String label, int soNguoi) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvLabel = new TextView(getActivity());
        tvLabel.setText(label);
        tvLabel.setTextSize(12);
        tvLabel.setTextColor(getResources().getColor(R.color.text_primary, null));
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvLabel.setMinWidth(dp(90));

        // Dots trực quan
        TextView tvDots = new TextView(getActivity());
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < soNguoi; i++) dots.append("●");
        tvDots.setText(dots.toString());
        tvDots.setTextSize(10);
        tvDots.setTextColor(getResources().getColor(R.color.teal_primary, null));
        tvDots.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvDots.setPadding(dp(6), 0, dp(6), 0);

        TextView tvCount = new TextView(getActivity());
        tvCount.setText(soNguoi + " người");
        tvCount.setTextSize(12);
        tvCount.setTypeface(null, Typeface.BOLD);
        tvCount.setTextColor(getResources().getColor(R.color.teal_primary, null));

        row.addView(tvLabel); row.addView(tvDots); row.addView(tvCount);
        containerTuoi.addView(row);
    }
    void taiSinhNhat() {
        containerSinhNhat.removeAllViews();
        tvTitleSinhNhat.setText("🎂  Sinh nhật tháng " + thangSinhNhat);

        List<NhanVien> dsNV = nhanVienDAO.layTatCa();
        int ngayHomNay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int thangHomNay = Calendar.getInstance().get(Calendar.MONTH) + 1;

        List<Object[]> sinhNhats = new ArrayList<>();
        for (NhanVien nv : dsNV) {
            int[] dm = layNgayThang(nv.getNgaySinh());
            if (dm == null) continue;
            if (dm[1] == thangSinhNhat) {
                sinhNhats.add(new Object[]{nv, dm[0]});
            }
        }
        // Sắp xếp theo ngày trong tháng
        Collections.sort(sinhNhats, (a, b) -> (int) a[1] - (int) b[1]);

        if (sinhNhats.isEmpty()) {
            TextView tv = new TextView(getActivity());
            tv.setText("Không có sinh nhật trong tháng " + thangSinhNhat);
            tv.setTextSize(12);
            tv.setTextColor(getResources().getColor(R.color.gray_text, null));
            containerSinhNhat.addView(tv);
            return;
        }

        for (Object[] item : sinhNhats) {
            NhanVien nv = (NhanVien) item[0];
            int ngaySN = (int) item[1];

            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(6), 0, dp(6));
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Avatar
            TextView tvA = new TextView(getActivity());
            String[] parts = nv.getTenNV().trim().split(" ");
            tvA.setText(parts[parts.length - 1].substring(0, 1).toUpperCase());
            tvA.setTextColor(getResources().getColor(R.color.white, null));
            tvA.setTextSize(14); tvA.setGravity(android.view.Gravity.CENTER);
            tvA.setBackground(getResources().getDrawable(R.drawable.bg_avatar, null));
            tvA.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));

            // Tên + ngày
            LinearLayout col = new LinearLayout(getActivity());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(dp(10), 0, 0, 0);
            col.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            TextView tvName = new TextView(getActivity());
            tvName.setText(nv.getTenNV()); tvName.setTextSize(13);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(getResources().getColor(R.color.text_primary, null));
            TextView tvDate = new TextView(getActivity());
            tvDate.setText(String.format("%02d/%02d", ngaySN, thangSinhNhat));
            tvDate.setTextSize(11);
            tvDate.setTextColor(getResources().getColor(R.color.gray_text, null));
            col.addView(tvName); col.addView(tvDate);

            // Badge
            TextView tvBadge = new TextView(getActivity());
            tvBadge.setTextSize(11);
            boolean isHomNay = (ngaySN == ngayHomNay && thangSinhNhat == thangHomNay);
            boolean daNgay = (thangSinhNhat == thangHomNay && ngaySN < ngayHomNay);
            if (isHomNay) {
                tvBadge.setText("Hôm nay");
                tvBadge.setTextColor(getResources().getColor(R.color.teal_primary, null));
                tvBadge.setTypeface(null, Typeface.BOLD);
            } else if (daNgay) {
                tvBadge.setText("Đã qua");
                tvBadge.setTextColor(getResources().getColor(R.color.gray_text, null));
            } else if (thangSinhNhat == thangHomNay) {
                tvBadge.setText("Còn " + (ngaySN - ngayHomNay) + " ngày");
                tvBadge.setTextColor(getResources().getColor(R.color.status_sap_het, null));
            } else {
                tvBadge.setText(String.format("%02d/%02d", ngaySN, thangSinhNhat));
                tvBadge.setTextColor(getResources().getColor(R.color.gray_text, null));
            }

            row.addView(tvA); row.addView(col); row.addView(tvBadge);
            containerSinhNhat.addView(row);
        }
    }


    // ── Tính tuổi từ chuỗi ngày sinh ──
    int tinhTuoi(String ngaySinh) {
        if (ngaySinh == null || !ngaySinh.contains("/")) return 0;
        try {
            String[] p = ngaySinh.split("/");
            int namSinh = Integer.parseInt(p[2]);
            int thangSinh = Integer.parseInt(p[1]);
            int ngay = Integer.parseInt(p[0]);
            Calendar now = Calendar.getInstance();
            int tuoi = now.get(Calendar.YEAR) - namSinh;
            if (now.get(Calendar.MONTH) + 1 < thangSinh ||
                    (now.get(Calendar.MONTH) + 1 == thangSinh && now.get(Calendar.DAY_OF_MONTH) < ngay)) {
                tuoi--;
            }
            return tuoi;
        } catch (Exception e) { return 0; }
    }

    // ── Lấy ngày/tháng từ chuỗi ngày sinh ──
    int[] layNgayThang(String ngaySinh) {
        if (ngaySinh == null || !ngaySinh.contains("/")) return null;
        try {
            String[] p = ngaySinh.split("/");
            return new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1])};
        } catch (Exception e) { return null; }
    }

    int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ══════════════════════════════════════
    // ══ TAB 1: THEO VỊ TRÍ (giữ nguyên logic cũ) ══
    // ══════════════════════════════════════

    public void taiTab1() {
        List<ViTri> dsViTri = viTriDAO.layTatCa();
        if (dsViTri.isEmpty()) {
            spinnerViTri.setVisibility(View.GONE);
            View frame = getView() != null ? getView().findViewById(R.id.frame_spinner_vi_tri) : null;
            if (frame != null) frame.setVisibility(View.GONE);
            tvKhongCoNv.setVisibility(View.VISIBLE);
            tvKhongCoNv.setText("Chưa có vị trí nào trong hệ thống");
            return;
        }

        List<String> tenViTri = new ArrayList<>();
        tenViTri.add("-- Chọn vị trí --");
        for (ViTri vt : dsViTri) {
            int soNv = nhanVienDAO.layTheoViTri(vt.getId()).size();
            tenViTri.add(vt.getTenVT() + " (" + vt.getMaVT() + ") — " + soNv + " NV");
        }
        tenViTri.add("👤 Chưa gán vị trí — " + nhanVienDAO.layNhanVienChuaGan().size() + " NV");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, tenViTri);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerViTri.setAdapter(adapter);

        tvSoNvTheoViTri.setText("");
        tvKhongCoNv.setVisibility(View.VISIBLE);
        tvKhongCoNv.setText("Chọn vị trí để xem danh sách nhân viên");
        lvNvTheoViTri.setVisibility(View.GONE);

        spinnerViTri.setSelection(viTriDangChon);
        spinnerViTri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viTriDangChon = position;
                if (position == 0) {
                    tvSoNvTheoViTri.setText("");
                    tvKhongCoNv.setVisibility(View.VISIBLE);
                    tvKhongCoNv.setText("Chọn vị trí để xem danh sách nhân viên");
                    lvNvTheoViTri.setVisibility(View.GONE);
                } else if (position == dsViTri.size() + 1) {
                    List<NhanVien> ds = nhanVienDAO.layNhanVienChuaGan();
                    tvSoNvTheoViTri.setText("Tổng: " + ds.size() + " nhân viên chưa gán");
                    if (ds.isEmpty()) {
                        lvNvTheoViTri.setVisibility(View.GONE);
                        tvKhongCoNv.setVisibility(View.VISIBLE);
                        tvKhongCoNv.setText("Tất cả nhân viên đã được gán vị trí");
                    } else {
                        tvKhongCoNv.setVisibility(View.GONE);
                        lvNvTheoViTri.setVisibility(View.VISIBLE);
                        hienThiDsNV(ds);
                    }
                } else {
                    hienThiNvTheoViTri(dsViTri.get(position - 1));
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    void hienThiNvTheoViTri(ViTri vt) {
        List<NhanVien> dsNV = nhanVienDAO.layTheoViTri(vt.getId());
        tvSoNvTheoViTri.setText("Tổng: " + dsNV.size() + " nhân viên");
        if (dsNV.isEmpty()) {
            lvNvTheoViTri.setVisibility(View.GONE);
            tvKhongCoNv.setVisibility(View.VISIBLE);
            tvKhongCoNv.setText("Chưa có nhân viên nào ở vị trí này");
        } else {
            tvKhongCoNv.setVisibility(View.GONE);
            lvNvTheoViTri.setVisibility(View.VISIBLE);
            hienThiDsNV(dsNV);
        }
    }

    void hienThiDsNV(List<NhanVien> ds) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < ds.size(); i++) {
            NhanVien nv = ds.get(i);
            items.add((i + 1) + ".  " + nv.getTenNV() + "  |  " + nv.getMaNV()
                    + "  |  " + nv.getQueQuan());
        }
        lvNvTheoViTri.setAdapter(new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1, items));
    }

    // ══════════════════════════════════════
    // ══ TAB 2: LƯƠNG > 10TR ══
    // ══════════════════════════════════════

    public void taiTab2() {
        tvLcThang.setText("Tháng " + String.format("%02d/%d", lcThang, lcNam));

        // Lấy lương đã tính từ BANG_LUONG
        LuongDAO luongDAO = new LuongDAO(getActivity());
        List<BangLuong> danhSach = luongDAO.layDanhSach(lcThang, lcNam);

        // Lọc lương thực nhận > 10 triệu
        List<BangLuong> dsLuongCao = new ArrayList<>();
        for (BangLuong bl : danhSach) {
            if (bl.getLuongThucNhan() > 10000000) {
                dsLuongCao.add(bl);
            }
        }

        tvTieuDeLuong.setText("Nhân viên có lương trên 10.000.000 đ — "
                + dsLuongCao.size() + " người");

        if (dsLuongCao.isEmpty()) {
            lvLuongCao.setVisibility(View.GONE);
            tvKhongCoLuongCao.setVisibility(View.VISIBLE);
            if (danhSach.isEmpty()) {
                tvKhongCoLuongCao.setText("Chưa tính lương tháng "
                        + String.format("%02d/%d", lcThang, lcNam)
                        + "\nVào tab Lương → nhấn TÍNH LƯƠNG trước");
            } else {
                tvKhongCoLuongCao.setText("Không có nhân viên nào lương trên 10 triệu trong tháng này");
            }
        } else {
            tvKhongCoLuongCao.setVisibility(View.GONE);
            lvLuongCao.setVisibility(View.VISIBLE);
            List<String> items = new ArrayList<>();
            for (int i = 0; i < dsLuongCao.size(); i++) {
                BangLuong bl = dsLuongCao.get(i);
                items.add((i + 1) + ".  " + bl.getTenNV() + " · " + bl.getMaNV()
                        + "\n     → " + bl.getTenViTri() + ": "
                        + nf.format((long) bl.getLuongThucNhan()) + " đ");
            }
            lvLuongCao.setAdapter(new ArrayAdapter<>(
                    getActivity(), android.R.layout.simple_list_item_1, items));
        }
    }

    // ══════════════════════════════════════

    @Override
    public void onResume() {
        super.onResume();
        taiTongQuan();
        taiTab1();
        taiTab2();
    }
}