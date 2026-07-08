package com.example.quanlyvitri;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NhanVienFragment extends Fragment {

    static final int REQUEST_CHON_ANH = 2001;
    android.widget.ImageView imgAvatarDangMo;
    android.widget.TextView tvAvatarDangMo;
    String[] duongDanAnhTam;

    ListView listView;
    EditText edtTimKiem;
    TextView fabThem;
    NhanVienDAO dao;
    NhanVienAdapter adapter;
    List<NhanVien> danhSach = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        taiDanhSach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nhan_vien, container, false);

        listView   = view.findViewById(R.id.listview_nhan_vien);
        edtTimKiem = view.findViewById(R.id.edt_tim_kiem);
        fabThem    = view.findViewById(R.id.fab_them);
        dao        = new NhanVienDAO(getActivity());

        taiDanhSach();

        edtTimKiem.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            public void afterTextChanged(Editable s) {}
        });

        fabThem.setOnClickListener(v -> moDialogThem());

        listView.setOnItemClickListener((parent, v, position, id) -> {
            NhanVien nv = adapter.getItem(position);
            Intent intent = new Intent(getActivity(), ChiTietNhanVienActivity.class);
            intent.putExtra("nhan_vien_id", nv.getId());
            startActivity(intent);
        });

        return view;
    }

    void taiDanhSach() {
        danhSach = dao.layTatCa();
        adapter  = new NhanVienAdapter(getActivity(), danhSach);
        listView.setAdapter(adapter);
    }

    void moDialogThem() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Thêm nhân viên");

        View form = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_nhan_vien, null);
        builder.setView(form);

        android.widget.TextView  tvAvatar   = form.findViewById(R.id.tv_avatar_dialog);
        android.widget.ImageView imgAvatar  = form.findViewById(R.id.img_avatar_dialog);
        android.widget.TextView  btnChonAnh = form.findViewById(R.id.btn_chon_anh_dialog);
        android.widget.EditText  edtTen     = form.findViewById(R.id.edt_ten_nv);
        android.widget.EditText edtNgay  = form.findViewById(R.id.edt_ngay);
        android.widget.EditText edtThang = form.findViewById(R.id.edt_thang);
        android.widget.EditText edtNam   = form.findViewById(R.id.edt_nam);
        android.widget.EditText  edtQueQuan = form.findViewById(R.id.edt_que_quan);
        android.widget.Spinner   spinnerVT  = form.findViewById(R.id.spinner_vi_tri_dialog);




        ViTriDAO viTriDAO = new ViTriDAO(getActivity());
        List<ViTri> dsViTri = viTriDAO.layTatCa();
        List<String> tenViTri = new ArrayList<>();
        tenViTri.add("-- Chưa chọn vị trí --");
        for (ViTri vt : dsViTri) tenViTri.add(vt.getTenVT() + " (" + vt.getMaVT() + ")");
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, tenViTri);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVT.setAdapter(spinnerAdapter);


        final String[] duongDanAnh = {""};


        edtTen.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            public void afterTextChanged(android.text.Editable s) {}
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (duongDanAnh[0].isEmpty()) {
                    String ten = s.toString().trim();
                    String[] parts = ten.split(" ");
                    String chu = parts[parts.length - 1].length() > 0
                            ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
                    tvAvatar.setText(chu);
                }
            }
        });


        btnChonAnh.setOnClickListener(v -> {
            imgAvatarDangMo  = imgAvatar;
            tvAvatarDangMo   = tvAvatar;
            duongDanAnhTam   = duongDanAnh;
            android.content.Intent intent = new android.content.Intent(
                    android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CHON_ANH);
        });

        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", null);
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String ten = edtTen.getText().toString().trim();
                    if (ten.isEmpty()) {
                        Toast.makeText(getActivity(), "Tên không được trống!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int viTriId = spinnerVT.getSelectedItemPosition() == 0
                            ? -1 : dsViTri.get(spinnerVT.getSelectedItemPosition() - 1).getId();
                    long result = dao.them(ten,
                            formatNgaySinh(edtNgay, edtThang, edtNam),
                            edtQueQuan.getText().toString().trim(),
                            viTriId,
                            duongDanAnh[0]);
                    if (result > 0) {
                        Toast.makeText(getActivity(), "Thêm thành công!",
                                Toast.LENGTH_SHORT).show();
                        taiDanhSach();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Có lỗi xảy ra!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHON_ANH
                && resultCode == android.app.Activity.RESULT_OK && data != null) {
            android.net.Uri uri = data.getData();
            try {
                getActivity().getContentResolver().takePersistableUriPermission(
                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                duongDanAnhTam[0] = uri.toString();
                java.io.InputStream stream = getActivity()
                        .getContentResolver().openInputStream(uri);
                android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory.decodeStream(stream);
                imgAvatarDangMo.setImageBitmap(bitmap);
                imgAvatarDangMo.setVisibility(View.VISIBLE);
                tvAvatarDangMo.setVisibility(View.GONE);
            } catch (Exception e) {
                android.widget.Toast.makeText(getActivity(),
                        "Không thể tải ảnh!", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    String formatNgaySinh(android.widget.EditText edtNgay,
                          android.widget.EditText edtThang,
                          android.widget.EditText edtNam) {
        String ngay  = edtNgay.getText().toString().trim();
        String thang = edtThang.getText().toString().trim();
        String nam   = edtNam.getText().toString().trim();
        if (ngay.isEmpty() && thang.isEmpty() && nam.isEmpty()) return "";
        // Format 2 chữ số cho ngày và tháng
        if (!ngay.isEmpty())  ngay  = String.format("%02d", Integer.parseInt(ngay));
        if (!thang.isEmpty()) thang = String.format("%02d", Integer.parseInt(thang));
        return ngay + "/" + thang + "/" + nam;
    }
}