package com.example.quanlyvitri;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ViTriFragment extends Fragment {

    ListView listView;
    TextView fabThem;
    ViTriDAO dao;
    List<ViTri> danhSach = new ArrayList<>();
    ArrayAdapter<ViTri> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vi_tri, container, false);

        listView = view.findViewById(R.id.listview_vi_tri);
        fabThem  = view.findViewById(R.id.fab_them_vt);
        dao      = new ViTriDAO(getActivity());

        taiDanhSach();

        fabThem.setOnClickListener(v -> moDialogThem());



        return view;
    }

    void taiDanhSach() {
        danhSach = dao.layTatCa();
        adapter  = new ViTriAdapter(getActivity(), danhSach, new ViTriAdapter.OnActionListener() {
            public void onSua(ViTri vt) { moDialogSuaXoa(vt); }
            public void onXoa(ViTri vt) {

                int soNv = dao.soNhanVienTheoViTri(vt.getId());
                if (soNv > 0) {
                    new android.app.AlertDialog.Builder(getActivity())
                            .setTitle("Không thể xóa")
                            .setMessage("Vị trí \"" + vt.getTenVT() + "\" đang có "
                                    + soNv + " nhân viên.\nVui lòng hủy gán nhân viên trước khi xóa!")
                            .setPositiveButton("Đã hiểu", null)
                            .show();
                    return;
                }
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Xóa vị trí " + vt.getTenVT() + "?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            dao.xoa(vt.getId());
                            android.widget.Toast.makeText(getActivity(), "Đã xóa!", android.widget.Toast.LENGTH_SHORT).show();
                            taiDanhSach();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        listView.setAdapter(adapter);
    }

    void moDialogThem() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Thêm vị trí");

        View form = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_vi_tri, null);
        builder.setView(form);


        android.widget.EditText edtTen   = form.findViewById(R.id.edt_ten_vt);
        android.widget.EditText edtLuong = form.findViewById(R.id.edt_luong);

        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String ten = edtTen.getText().toString().trim();
                    if (ten.isEmpty()) {
                        Toast.makeText(getActivity(), "Tên vị trí không được trống!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String luongStr = edtLuong.getText().toString().trim();
                    if (luongStr.isEmpty()) {
                        Toast.makeText(getActivity(), "Mức lương không được trống!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double luong = Double.parseDouble(luongStr);
                    if (luong <= 0) {
                        Toast.makeText(getActivity(), "Mức lương phải lớn hơn 0!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    android.widget.EditText edtSNL = form.findViewById(R.id.edt_so_nhan_luc);
                    String sNL = edtSNL.getText().toString().trim();
                    int soNhanLuc = sNL.isEmpty() ? 1 : Integer.parseInt(sNL);
                    long result = dao.them(ten, luong, soNhanLuc);
                    if (result > 0) {
                        Toast.makeText(getActivity(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        taiDanhSach();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void moDialogSuaXoa(ViTri vt) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Chỉnh sửa: " + vt.getTenVT());

        View form = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_vi_tri, null);
        builder.setView(form);

        android.widget.TextView tvMaHienThi = form.findViewById(R.id.tv_ma_vt_hien_thi);
        android.widget.EditText edtTen      = form.findViewById(R.id.edt_ten_vt);
        android.widget.EditText edtLuong    = form.findViewById(R.id.edt_luong);


        tvMaHienThi.setText("Mã: " + vt.getMaVT());
        edtTen.setText(vt.getTenVT());
        edtLuong.setText(String.format("%.0f", vt.getMucLuong()));
        android.widget.EditText edtSoNhanLuc = form.findViewById(R.id.edt_so_nhan_luc);
        edtSoNhanLuc.setText(String.valueOf(vt.getSoNhanLuc()));

        builder.setPositiveButton("Lưu", null); // null để không tự đóng
        builder.setNegativeButton("Hủy", null);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String ten = edtTen.getText().toString().trim();
                    if (ten.isEmpty()) {
                        Toast.makeText(getActivity(), "Tên vị trí không được trống!",
                                Toast.LENGTH_SHORT).show();
                        return; // giữ nguyên dialog
                    }
                    String luongStr = edtLuong.getText().toString().trim();
                    if (luongStr.isEmpty()) {
                        Toast.makeText(getActivity(), "Mức lương không được trống!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double luong = Double.parseDouble(luongStr);
                    if (luong <= 0) {
                        Toast.makeText(getActivity(), "Mức lương phải lớn hơn 0!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String sNL = edtSoNhanLuc.getText().toString().trim();
                    int soNhanLuc = sNL.isEmpty() ? 1 : Integer.parseInt(sNL);
                    int result = dao.sua(vt.getId(), vt.getMaVT(),
                            ten, luong, soNhanLuc);
                    if (result > 0) {
                        Toast.makeText(getActivity(), "Cập nhật thành công!",
                                Toast.LENGTH_SHORT).show();
                        taiDanhSach();
                        dialog.dismiss(); // đóng dialog sau khi lưu thành công
                    }
                });
    }
}