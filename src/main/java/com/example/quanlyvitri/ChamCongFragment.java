package com.example.quanlyvitri;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChamCongFragment extends Fragment {

    ChamCongDAO chamCongDAO;
    NhanVienDAO nhanVienDAO;
    Calendar ngayHienTai;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    NgayNghiLeDAO ngayNghiLeDAO;
    LinearLayout bannerNgayDacBiet;
    TextView tvBannerNgay, btnToggleNghiLe;

    TextView tvNgayHienThi;
    EditText tvNgay, tvThang, tvNam;

    TextView tvThongBao, tvCoMat, tvNghiPhep, tvVang, tvDaCham;
    LinearLayout containerDS;

    String[] thuVN = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư",
            "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
    String[] trangThaiOptions = {"Chưa chấm", "● Có mặt", "● Nghỉ phép", "● Vắng", "● Nghỉ lễ"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cham_cong, container, false);

        chamCongDAO = new ChamCongDAO(getActivity());
        nhanVienDAO = new NhanVienDAO(getActivity());
        ngayHienTai = Calendar.getInstance();

        // Ánh xạ view
        tvNgayHienThi = view.findViewById(R.id.tv_ngay_hien_thi);
        tvNgay   = view.findViewById(R.id.tv_ngay);
        tvThang  = view.findViewById(R.id.tv_thang);
        tvNam    = view.findViewById(R.id.tv_nam);
        tvThongBao = view.findViewById(R.id.tv_thong_bao);
        tvCoMat    = view.findViewById(R.id.tv_co_mat);
        tvNghiPhep = view.findViewById(R.id.tv_nghi_phep);
        tvVang     = view.findViewById(R.id.tv_vang);
        tvDaCham   = view.findViewById(R.id.tv_da_cham);
        containerDS = view.findViewById(R.id.container_ds_cham_cong);
        ngayNghiLeDAO = new NgayNghiLeDAO(getActivity());
        bannerNgayDacBiet = view.findViewById(R.id.banner_ngay_dac_biet);
        tvBannerNgay      = view.findViewById(R.id.tv_banner_ngay);
        btnToggleNghiLe   = view.findViewById(R.id.btn_toggle_nghi_le);

        btnToggleNghiLe.setOnClickListener(v -> {
            String ngayStr = sdf.format(ngayHienTai.getTime());
            if (ngayNghiLeDAO.laNgayLe(ngayStr)) {
                ngayNghiLeDAO.boDanhDau(ngayStr);
                Toast.makeText(getActivity(), "Đã bỏ đánh dấu nghỉ lễ", Toast.LENGTH_SHORT).show();
            } else {
                ngayNghiLeDAO.danhDau(ngayStr);
                Toast.makeText(getActivity(), "Đã đánh dấu nghỉ lễ", Toast.LENGTH_SHORT).show();
            }
            capNhatBanner();
        });
        view.findViewById(R.id.btn_hom_nay).setOnClickListener(v -> {
            ngayHienTai = Calendar.getInstance();
            taiDuLieu();
        });
        view.findViewById(R.id.btn_tai_ngay).setOnClickListener(v -> {
            if (docNgayTuO()) {
                taiDuLieu();
                // Ẩn bàn phím
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getActivity()
                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        // Điều hướng ngày
        view.findViewById(R.id.btn_ngay_truoc).setOnClickListener(v -> doiNgay(-1));
        view.findViewById(R.id.btn_ngay_sau).setOnClickListener(v -> doiNgay(1));

        // Nút nhanh
        view.findViewById(R.id.btn_tat_ca_co_mat).setOnClickListener(v -> tatCaCoMat());
        view.findViewById(R.id.btn_chep_ngay_truoc).setOnClickListener(v -> chepNgayTruoc());
        view.findViewById(R.id.btn_tat_ca_nghi_le).setOnClickListener(v -> tatCaNghiLe());
        view.findViewById(R.id.btn_cham_hang_loat).setOnClickListener(v -> moDialogChamHangLoat());

        // Lưu
        view.findViewById(R.id.btn_luu_cham_cong).setOnClickListener(v -> luuChamCong());


        taiDuLieu();
        return view;
    }

    // ══ Tải danh sách nhân viên + chấm công cho ngày đang chọn ══
    void taiDuLieu() {
        // Cập nhật header ngày
        String ngayStr = sdf.format(ngayHienTai.getTime());
        int thu = ngayHienTai.get(Calendar.DAY_OF_WEEK);
        tvNgayHienThi.setText(thuVN[thu - 1] + ", " + ngayStr);

        tvNgay.setText(String.format("%02d", ngayHienTai.get(Calendar.DAY_OF_MONTH)));
        tvThang.setText(String.format("%02d", ngayHienTai.get(Calendar.MONTH) + 1));
        tvNam.setText(String.valueOf(ngayHienTai.get(Calendar.YEAR)));

        capNhatBanner();

        // Lấy dữ liệu chấm công đã lưu
        Map<Integer, Integer> chamCong = chamCongDAO.layMapTheoNgay(ngayStr);

        // Lấy danh sách nhân viên
        List<NhanVien> dsNV = nhanVienDAO.layTatCa();

        // Xây dựng các dòng nhân viên
        containerDS.removeAllViews();
        for (NhanVien nv : dsNV) {
            View row = LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_cham_cong, containerDS, false);
            row.setTag(nv.getId()); // Lưu id để dùng khi save

            // Avatar chữ cái đầu
            String[] parts = nv.getTenNV().trim().split(" ");
            String chuDau = parts[parts.length - 1].length() > 0
                    ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
            ((TextView) row.findViewById(R.id.tv_avatar_cc)).setText(chuDau);
            ((TextView) row.findViewById(R.id.tv_ten_cc)).setText(nv.getTenNV());
            ((TextView) row.findViewById(R.id.tv_ma_cc)).setText(nv.getMaNV());

            // Spinner trạng thái
            Spinner spinner = row.findViewById(R.id.spinner_trang_thai);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, trangThaiOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // Đặt trạng thái đã lưu (nếu có)
            Integer tt = chamCong.get(nv.getId());
            spinner.setSelection(tt != null ? tt : 0);

            // Cập nhật thống kê realtime khi user đổi spinner
            spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> p, View v,
                                           int pos, long id) {
                    capNhatThongKe();
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> p) {}
            });

            containerDS.addView(row);
        }

        capNhatThongKe();
    }

    // ══ Đếm trạng thái từ các Spinner hiện tại — cập nhật realtime ══
    void capNhatThongKe() {
        int coMat = 0, nghiPhep = 0, vang = 0, nghiLe = 0, daCham = 0;
        int tongNV = containerDS.getChildCount();

        for (int i = 0; i < tongNV; i++) {
            Spinner sp = containerDS.getChildAt(i).findViewById(R.id.spinner_trang_thai);
            int pos = sp.getSelectedItemPosition();
            if (pos == 1) { coMat++; daCham++; }
            else if (pos == 2) { nghiPhep++; daCham++; }
            else if (pos == 3) { vang++; daCham++; }
            else if (pos == 4) { nghiLe++; daCham++; }
        }

        tvCoMat.setText(String.valueOf(coMat + nghiLe));  // Nghỉ lễ gộp vào "Có mặt" vì tính công
        tvNghiPhep.setText(String.valueOf(nghiPhep));
        tvVang.setText(String.valueOf(vang));
        tvDaCham.setText("Đã chấm " + daCham + "/" + tongNV);

        if (daCham == 0) {
            tvThongBao.setText("⚠ Chưa có dữ liệu");
            tvThongBao.setVisibility(View.VISIBLE);
        } else {
            tvThongBao.setVisibility(View.GONE);
        }
    }

    // ══ Chuyển ngày ◄ ► ══
    void doiNgay(int delta) {
        docNgayTuO(); // Đọc trước, nếu lỗi thì giữ ngày cũ
        ngayHienTai.add(Calendar.DAY_OF_MONTH, delta);
        taiDuLieu();
    }

    boolean docNgayTuO() {
        try {
            String sN = tvNgay.getText().toString().trim();
            String sT = tvThang.getText().toString().trim();
            String sY = tvNam.getText().toString().trim();

            if (sN.isEmpty() || sT.isEmpty() || sY.isEmpty()) {
                Toast.makeText(getActivity(), "Vui lòng nhập đủ ngày/tháng/năm",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            int n = Integer.parseInt(sN);
            int t = Integer.parseInt(sT);
            int y = Integer.parseInt(sY);

            if (t < 1 || t > 12) {
                Toast.makeText(getActivity(), "Tháng phải từ 1 đến 12",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (y < 2000 || y > 2100) {
                Toast.makeText(getActivity(), "Năm phải từ 2000 đến 2100",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            // Kiểm tra ngày hợp lệ theo tháng/năm (xử lý 29/2, tháng 30/31 ngày)
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, t - 1);
            int maxNgay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (n < 1 || n > maxNgay) {
                Toast.makeText(getActivity(),
                        "Tháng " + t + "/" + y + " chỉ có " + maxNgay + " ngày!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            ngayHienTai.set(y, t - 1, n);
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Ngày/tháng/năm không hợp lệ",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // ══ Đặt tất cả spinner = "Có mặt" ══
    void tatCaCoMat() {
        for (int i = 0; i < containerDS.getChildCount(); i++) {
            Spinner sp = containerDS.getChildAt(i).findViewById(R.id.spinner_trang_thai);
            sp.setSelection(1);
        }
    }

    // ══ Chép dữ liệu từ ngày hôm trước ══
    void chepNgayTruoc() {
        Calendar cal = (Calendar) ngayHienTai.clone();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String ngayTruoc = sdf.format(cal.getTime());
        Map<Integer, Integer> dataTruoc = chamCongDAO.layMapTheoNgay(ngayTruoc);

        if (dataTruoc.isEmpty()) {
            Toast.makeText(getActivity(), "Ngày trước chưa có dữ liệu chấm công!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < containerDS.getChildCount(); i++) {
            View row = containerDS.getChildAt(i);
            int nvId = (int) row.getTag();
            Spinner sp = row.findViewById(R.id.spinner_trang_thai);
            Integer tt = dataTruoc.get(nvId);
            sp.setSelection(tt != null ? tt : 0);
        }
        Toast.makeText(getActivity(), "Đã chép từ " + ngayTruoc, Toast.LENGTH_SHORT).show();
    }

    // ══ Lưu tất cả chấm công vào database ══
    void luuChamCong() {
        String ngayStr = sdf.format(ngayHienTai.getTime());
        Map<Integer, Integer> data = new HashMap<>();

        for (int i = 0; i < containerDS.getChildCount(); i++) {
            View row = containerDS.getChildAt(i);
            int nvId = (int) row.getTag();
            Spinner sp = row.findViewById(R.id.spinner_trang_thai);
            int pos = sp.getSelectedItemPosition();
            if (pos > 0) {
                data.put(nvId, pos); // pos 1=có mặt, 2=nghỉ phép, 3=vắng
            }
        }

        if (data.isEmpty()) {
            Toast.makeText(getActivity(), "Chưa chấm cho ai!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        chamCongDAO.luuTatCa(ngayStr, data);
        Toast.makeText(getActivity(), "Đã lưu chấm công ngày " + ngayStr + "!",
                Toast.LENGTH_SHORT).show();
    }
    void capNhatBanner() {
        String ngayStr = sdf.format(ngayHienTai.getTime());
        int dow = ngayHienTai.get(Calendar.DAY_OF_WEEK);
        boolean laCuoiTuan = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
        boolean laNgayLe = ngayNghiLeDAO.laNgayLe(ngayStr);

        bannerNgayDacBiet.setVisibility(View.VISIBLE);

        if (laNgayLe && laCuoiTuan) {
            tvBannerNgay.setText("🎌 Nghỉ lễ + Cuối tuần — Tăng ca ×3");
            tvBannerNgay.setTextColor(getActivity().getResources().getColor(R.color.status_het_han, null));
            btnToggleNghiLe.setText("Bỏ nghỉ lễ");
            btnToggleNghiLe.setTextColor(getActivity().getResources().getColor(R.color.status_het_han, null));
        } else if (laNgayLe) {
            tvBannerNgay.setText("🎌 Ngày nghỉ lễ — Tăng ca ×3");
            tvBannerNgay.setTextColor(getActivity().getResources().getColor(R.color.status_het_han, null));
            btnToggleNghiLe.setText("Bỏ nghỉ lễ");
            btnToggleNghiLe.setTextColor(getActivity().getResources().getColor(R.color.status_het_han, null));
        } else if (laCuoiTuan) {
            tvBannerNgay.setText("📅 Cuối tuần — Tăng ca ×2");
            tvBannerNgay.setTextColor(getActivity().getResources().getColor(R.color.status_sap_het, null));
            btnToggleNghiLe.setText("Đánh dấu nghỉ lễ");
            btnToggleNghiLe.setTextColor(getActivity().getResources().getColor(R.color.teal_primary, null));
        } else {
            tvBannerNgay.setText("Ngày làm việc bình thường");
            tvBannerNgay.setTextColor(getActivity().getResources().getColor(R.color.gray_text, null));
            btnToggleNghiLe.setText("Đánh dấu nghỉ lễ");
            btnToggleNghiLe.setTextColor(getActivity().getResources().getColor(R.color.teal_primary, null));
        }
    }
    void tatCaNghiLe() {
        for (int i = 0; i < containerDS.getChildCount(); i++) {
            Spinner sp = containerDS.getChildAt(i).findViewById(R.id.spinner_trang_thai);
            sp.setSelection(4); // Nghỉ lễ
        }
    }

    void moDialogChamHangLoat() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Chấm công hàng loạt");

        // Tạo form bằng code
        LinearLayout form = new LinearLayout(getActivity());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(16), dp(20), dp(8));

        TextView tvMoTa = new TextView(getActivity());
        tvMoTa.setText("Tự động chấm:\n• Ngày thường → Có mặt\n• Ngày lễ → Nghỉ lễ\n• T7/CN → Bỏ qua");
        tvMoTa.setTextSize(12);
        tvMoTa.setTextColor(getActivity().getResources().getColor(R.color.gray_text, null));
        tvMoTa.setPadding(0, 0, 0, dp(12));
        form.addView(tvMoTa);

        // Từ ngày
        TextView lblTu = new TextView(getActivity());
        lblTu.setText("Từ ngày");
        lblTu.setTextSize(13);
        lblTu.setTypeface(null, Typeface.BOLD);
        form.addView(lblTu);

        LinearLayout rowTu = taoRowNgay();
        form.addView(rowTu);

        // Đến ngày
        TextView lblDen = new TextView(getActivity());
        lblDen.setText("Đến ngày");
        lblDen.setTextSize(13);
        lblDen.setTypeface(null, Typeface.BOLD);
        lblDen.setPadding(0, dp(12), 0, 0);
        form.addView(lblDen);

        LinearLayout rowDen = taoRowNgay();
        form.addView(rowDen);

        builder.setView(form);

        // Điền mặc định: ngày 1 → cuối tháng hiện tại
        EditText edtNgayTu  = (EditText) rowTu.getChildAt(0);
        EditText edtThangTu = (EditText) rowTu.getChildAt(2);
        EditText edtNamTu   = (EditText) rowTu.getChildAt(4);
        EditText edtNgayDen = (EditText) rowDen.getChildAt(0);
        EditText edtThangDen = (EditText) rowDen.getChildAt(2);
        EditText edtNamDen  = (EditText) rowDen.getChildAt(4);

        int thang = ngayHienTai.get(Calendar.MONTH) + 1;
        int nam = ngayHienTai.get(Calendar.YEAR);
        Calendar calCuoi = Calendar.getInstance();
        calCuoi.set(nam, thang - 1, 1);
        int ngayCuoi = calCuoi.getActualMaximum(Calendar.DAY_OF_MONTH);

        edtNgayTu.setText("01"); edtThangTu.setText(String.format("%02d", thang)); edtNamTu.setText(String.valueOf(nam));
        edtNgayDen.setText(String.valueOf(ngayCuoi)); edtThangDen.setText(String.format("%02d", thang)); edtNamDen.setText(String.valueOf(nam));

        builder.setPositiveButton("Chấm", null);
        builder.setNegativeButton("Hủy", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                int nTu = Integer.parseInt(edtNgayTu.getText().toString().trim());
                int tTu = Integer.parseInt(edtThangTu.getText().toString().trim());
                int yTu = Integer.parseInt(edtNamTu.getText().toString().trim());
                int nDen = Integer.parseInt(edtNgayDen.getText().toString().trim());
                int tDen = Integer.parseInt(edtThangDen.getText().toString().trim());
                int yDen = Integer.parseInt(edtNamDen.getText().toString().trim());

                Calendar calTu = Calendar.getInstance();
                calTu.set(yTu, tTu - 1, nTu);
                Calendar calDen = Calendar.getInstance();
                calDen.set(yDen, tDen - 1, nDen);

                if (!calDen.after(calTu) && !sdf.format(calTu.getTime()).equals(sdf.format(calDen.getTime()))) {
                    Toast.makeText(getActivity(), "Ngày kết thúc phải sau ngày bắt đầu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Duyệt từng ngày, phân loại
                List<NhanVien> dsNV = nhanVienDAO.layTatCa();
                List<Integer> dsNvId = new ArrayList<>();
                for (NhanVien nv : dsNV) dsNvId.add(nv.getId());

                java.util.LinkedHashMap<String, Integer> ngayMap = new java.util.LinkedHashMap<>();
                Calendar cal = (Calendar) calTu.clone();
                int soNgayThuong = 0, soNgayLe = 0, soBoqua = 0;

                while (!cal.after(calDen)) {
                    String ngayStr = sdf.format(cal.getTime());
                    int dow = cal.get(Calendar.DAY_OF_WEEK);
                    boolean cuoiTuan = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
                    boolean ngayLe = ngayNghiLeDAO.laNgayLe(ngayStr);

                    if (ngayLe) {
                        ngayMap.put(ngayStr, 4); // Nghỉ lễ
                        soNgayLe++;
                    } else if (!cuoiTuan) {
                        ngayMap.put(ngayStr, 1); // Có mặt
                        soNgayThuong++;
                    } else {
                        soBoqua++; // T7/CN bỏ qua
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (ngayMap.isEmpty()) {
                    Toast.makeText(getActivity(), "Không có ngày nào cần chấm!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Xác nhận
                int finalSoNgayThuong = soNgayThuong, finalSoNgayLe = soNgayLe, finalSoBoqua = soBoqua;
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("Xác nhận chấm hàng loạt")
                        .setMessage("• " + finalSoNgayThuong + " ngày thường → Có mặt"
                                + "\n• " + finalSoNgayLe + " ngày lễ → Nghỉ lễ"
                                + "\n• " + finalSoBoqua + " ngày T7/CN → Bỏ qua"
                                + "\n\nÁp dụng cho " + dsNvId.size() + " nhân viên"
                                + "\nTổng: " + (ngayMap.size() * dsNvId.size()) + " bản ghi")
                        .setPositiveButton("Chấm", (d2, w2) -> {
                            int count = chamCongDAO.chamHangLoat(ngayMap, dsNvId);
                            Toast.makeText(getActivity(),
                                    "Đã chấm " + count + " bản ghi!", Toast.LENGTH_SHORT).show();
                            taiDuLieu(); // Reload ngày hiện tại
                            dialog.dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper: tạo 1 hàng nhập ngày (3 ô)
    LinearLayout taoRowNgay() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        EditText edtN = new EditText(getActivity());
        edtN.setHint("DD"); edtN.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtN.setGravity(android.view.Gravity.CENTER); edtN.setTextSize(14);
        edtN.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView s1 = new TextView(getActivity());
        s1.setText(" / "); s1.setTextSize(16);

        EditText edtT = new EditText(getActivity());
        edtT.setHint("MM"); edtT.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtT.setGravity(android.view.Gravity.CENTER); edtT.setTextSize(14);
        edtT.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView s2 = new TextView(getActivity());
        s2.setText(" / "); s2.setTextSize(16);

        EditText edtY = new EditText(getActivity());
        edtY.setHint("YYYY"); edtY.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtY.setGravity(android.view.Gravity.CENTER); edtY.setTextSize(14);
        edtY.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));

        row.addView(edtN); row.addView(s1); row.addView(edtT); row.addView(s2); row.addView(edtY);
        return row;
    }
    int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density + 0.5f);
    }
}