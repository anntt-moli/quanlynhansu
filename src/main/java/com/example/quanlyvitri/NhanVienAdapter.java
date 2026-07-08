package com.example.quanlyvitri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class NhanVienAdapter extends ArrayAdapter<NhanVien> {

    private List<NhanVien> danhSachGoc;
    private List<NhanVien> danhSachHienThi;
    private HopDongDAO hopDongDAO; // ── MỚI

    public NhanVienAdapter(Context context, List<NhanVien> danhSach) {
        super(context, 0, danhSach);
        this.danhSachGoc     = new ArrayList<>(danhSach);
        this.danhSachHienThi = new ArrayList<>(danhSach);
        this.hopDongDAO      = new HopDongDAO(context); // ── MỚI
    }

    @Override
    public int getCount() { return danhSachHienThi.size(); }

    @Override
    public NhanVien getItem(int position) { return danhSachHienThi.get(position); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_nhan_vien, parent, false);
            holder = new ViewHolder();
            holder.tvStt       = convertView.findViewById(R.id.tv_stt);
            holder.tvAvatar    = convertView.findViewById(R.id.tv_avatar);
            holder.tvTen       = convertView.findViewById(R.id.tv_ten_nv);
            holder.tvMa        = convertView.findViewById(R.id.tv_ma_nv);
            holder.tvViTri     = convertView.findViewById(R.id.tv_vi_tri_nv);
            holder.tvTrangThai = convertView.findViewById(R.id.tv_trang_thai_hd); // ── MỚI
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NhanVien nv = danhSachHienThi.get(position);

        holder.tvStt.setText(String.valueOf(position + 1));

        String[] parts = nv.getTenNV().trim().split(" ");
        String chuDau = parts[parts.length - 1].length() > 0
                ? String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase() : "?";
        holder.tvAvatar.setText(chuDau);
        holder.tvTen.setText(nv.getTenNV());
        holder.tvMa.setText("MS: " + nv.getMaNV());
        holder.tvViTri.setText("Quê: " + nv.getQueQuan());

        // ══ MỚI: Hiển thị trạng thái hợp đồng ══
        HopDong hd = hopDongDAO.layHopDongHieuLuc(nv.getId());
        if (hd == null) {
            // Chưa có hợp đồng nào
            holder.tvTrangThai.setText("⚠ Chưa có hợp đồng");
            holder.tvTrangThai.setTextColor(
                    getContext().getResources().getColor(R.color.status_chua_co, null));
        } else {
            String[] tt = hd.getTrangThai(); // [0]=text, [1]="xanh"/"vang"/"do"
            int mau;
            String icon;
            switch (tt[1]) {
                case "do":
                    mau = R.color.status_het_han;
                    icon = "⊘ ";
                    break;
                case "vang":
                    mau = R.color.status_sap_het;
                    icon = "⚠ ";
                    break;
                default: // xanh
                    mau = R.color.status_con_han;
                    icon = "✓ ";
                    break;
            }
            // Thêm prefix "Hợp đồng" cho rõ nghĩa trên danh sách
            String text = tt[0]; // "Còn 208 ngày", "Đã hết hạn", "Không thời hạn"
            holder.tvTrangThai.setText(icon + "Hợp đồng " + text.toLowerCase());
            holder.tvTrangThai.setTextColor(
                    getContext().getResources().getColor(mau, null));
        }
        holder.tvTrangThai.setVisibility(View.VISIBLE);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<NhanVien> ketQua = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    ketQua.addAll(danhSachGoc);
                } else {
                    String keyword = constraint.toString().toLowerCase().trim();
                    for (NhanVien nv : danhSachGoc) {
                        if (nv.getTenNV().toLowerCase().contains(keyword)
                                || nv.getMaNV().toLowerCase().contains(keyword)) {
                            ketQua.add(nv);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = ketQua;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                danhSachHienThi.clear();
                danhSachHienThi.addAll((List<NhanVien>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class ViewHolder {
        TextView tvStt, tvAvatar, tvTen, tvMa, tvViTri;
        TextView tvTrangThai; // ── MỚI
    }
}