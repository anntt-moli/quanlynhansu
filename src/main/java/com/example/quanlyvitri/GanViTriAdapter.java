package com.example.quanlyvitri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GanViTriAdapter extends ArrayAdapter<ViTri> {

    public interface OnHuyGanListener {
        void onHuyGan(ViTri viTri);
    }

    private OnHuyGanListener listener;

    public GanViTriAdapter(Context context, List<ViTri> danhSach, OnHuyGanListener listener) {
        super(context, 0, danhSach);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_gan_vitri, parent, false);

        ViTri vt = getItem(position);

        TextView tvTen   = convertView.findViewById(R.id.tv_ten_vt_gan);
        TextView tvLuong = convertView.findViewById(R.id.tv_luong_vt_gan);
        TextView btnHuy  = convertView.findViewById(R.id.btn_huy_gan);

        tvTen.setText(vt.getTenVT() + " (" + vt.getMaVT() + ")");
        String luongFormat = NumberFormat.getNumberInstance(Locale.US)
                .format(vt.getMucLuong()) + " đ";
        tvLuong.setText(luongFormat);

        btnHuy.setOnClickListener(v -> {
            if (listener != null) listener.onHuyGan(vt);
        });

        return convertView;
    }
}