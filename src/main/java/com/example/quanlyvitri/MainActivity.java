package com.example.quanlyvitri;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 5 tab thay vì 3
    LinearLayout tabNhanVien, tabViTri, tabChamCong, tabLuong, tabBaoCao;
    ImageView iconNhanVien, iconViTri, iconChamCong, iconLuong, iconBaoCao;
    TextView textNhanVien, textViTri, textChamCong, textLuong, textBaoCao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view — 5 tab
        tabNhanVien = findViewById(R.id.tab_nhan_vien);
        tabViTri    = findViewById(R.id.tab_vi_tri);
        tabChamCong = findViewById(R.id.tab_cham_cong);   // MỚI
        tabLuong    = findViewById(R.id.tab_luong);         // MỚI
        tabBaoCao   = findViewById(R.id.tab_bao_cao);

        iconNhanVien = findViewById(R.id.icon_nhan_vien);
        iconViTri    = findViewById(R.id.icon_vi_tri);
        iconChamCong = findViewById(R.id.icon_cham_cong);   // MỚI
        iconLuong    = findViewById(R.id.icon_luong);        // MỚI
        iconBaoCao   = findViewById(R.id.icon_bao_cao);

        textNhanVien = findViewById(R.id.text_nhan_vien);
        textViTri    = findViewById(R.id.text_vi_tri);
        textChamCong = findViewById(R.id.text_cham_cong);   // MỚI
        textLuong    = findViewById(R.id.text_luong);        // MỚI
        textBaoCao   = findViewById(R.id.text_bao_cao);

        // Mở tab Nhân Viên mặc định
        selectTab(0);

        // Xử lý click — 5 tab
        tabNhanVien.setOnClickListener(v -> selectTab(0));
        tabViTri.setOnClickListener(v -> selectTab(1));
        tabChamCong.setOnClickListener(v -> selectTab(2));  // MỚI
        tabLuong.setOnClickListener(v -> selectTab(3));      // MỚI
        tabBaoCao.setOnClickListener(v -> {
            selectTab(4);
            getFragmentManager().executePendingTransactions();
            android.app.Fragment f = getFragmentManager().findFragmentById(R.id.frame_container);
            if (f instanceof BaoCaoFragment) {
                ((BaoCaoFragment) f).taiTongQuan(); // ← THÊM
                ((BaoCaoFragment) f).taiTab1();
                ((BaoCaoFragment) f).taiTab2();
            }
        });
    }

    private void selectTab(int index) {
        int gray = getResources().getColor(R.color.gray_text, null);
        int teal = getResources().getColor(R.color.teal_primary, null);

        // Reset tất cả về xám
        iconNhanVien.setColorFilter(gray);
        iconViTri.setColorFilter(gray);
        iconChamCong.setColorFilter(gray);  // MỚI
        iconLuong.setColorFilter(gray);      // MỚI
        iconBaoCao.setColorFilter(gray);

        textNhanVien.setTextColor(gray);
        textViTri.setTextColor(gray);
        textChamCong.setTextColor(gray);    // MỚI
        textLuong.setTextColor(gray);        // MỚI
        textBaoCao.setTextColor(gray);

        // Highlight tab được chọn + tạo Fragment tương ứng
        android.app.Fragment fragment;
        switch (index) {
            case 1:
                iconViTri.setColorFilter(teal);
                textViTri.setTextColor(teal);
                fragment = new ViTriFragment();
                break;
            case 2:  // MỚI
                iconChamCong.setColorFilter(teal);
                textChamCong.setTextColor(teal);
                fragment = new ChamCongFragment();
                break;
            case 3:  // MỚI
                iconLuong.setColorFilter(teal);
                textLuong.setTextColor(teal);
                fragment = new LuongFragment();
                break;
            case 4:  // ← Báo Cáo đổi từ case 2 thành case 4
                iconBaoCao.setColorFilter(teal);
                textBaoCao.setTextColor(teal);
                fragment = new BaoCaoFragment();
                break;
            default:
                iconNhanVien.setColorFilter(teal);
                textNhanVien.setTextColor(teal);
                fragment = new NhanVienFragment();
                break;
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}