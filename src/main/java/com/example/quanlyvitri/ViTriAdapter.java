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

public class ViTriAdapter extends ArrayAdapter<ViTri> {

    public interface OnActionListener {
        void onSua(ViTri vt);
        void onXoa(ViTri vt);
    }

    private OnActionListener listener;

    public ViTriAdapter(Context context, List<ViTri> danhSach, OnActionListener listener) {
        super(context, 0, danhSach);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_vi_tri, parent, false);
            holder = new ViewHolder();
            holder.tvMa    = convertView.findViewById(R.id.tv_ma_vt);
            holder.tvTen   = convertView.findViewById(R.id.tv_ten_vt);
            holder.tvLuong = convertView.findViewById(R.id.tv_luong_vt);
            holder.btnSua  = convertView.findViewById(R.id.btn_sua_vt);
            holder.btnXoa  = convertView.findViewById(R.id.btn_xoa_vt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView tvMa    = holder.tvMa;
        TextView tvTen   = holder.tvTen;
        TextView tvLuong = holder.tvLuong;
        TextView btnSua  = holder.btnSua;
        TextView btnXoa  = holder.btnXoa;
        ViTri vt = getItem(position);

        tvMa.setText(vt.getMaVT());
        tvTen.setText(vt.getTenVT());

        long luongTrieu = (long)(vt.getMucLuong() / 1000000);
        tvLuong.setText(luongTrieu + " tr/tháng");

        if (vt.getMucLuong() > 10000000) {
            tvLuong.setTextColor(getContext().getResources().getColor(R.color.badge_high, null));
        } else {
            tvLuong.setTextColor(getContext().getResources().getColor(R.color.teal_primary, null));
        }

        btnSua.setOnClickListener(v -> { if (listener != null) listener.onSua(vt); });
        btnXoa.setOnClickListener(v -> { if (listener != null) listener.onXoa(vt); });

        return convertView;
    }

    static class ViewHolder {
        TextView tvMa, tvTen, tvLuong, btnSua, btnXoa;
    }
}