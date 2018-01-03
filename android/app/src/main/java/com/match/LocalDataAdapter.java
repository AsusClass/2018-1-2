package com.match;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class LocalDataAdapter extends RecyclerView.Adapter<LocalDataAdapter.ItemViewHolder> {

    List<LocalData> list = new ArrayList<>();

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void refresh(LocalData data) {
        list.add(data);
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, null));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final LocalData data = list.get(position);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        holder.time.setText("\t匹配时间 " + data.match_time / 1000000f + "ms");
        holder.store.setText("\t加载时间 " + nf.format(data.sotre_time / 1000000f) + "ms");
        holder.num.setText("\t个数 " + data.match_num + " 个");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView store, num, time;

        public ItemViewHolder(View itemView) {
            super(itemView);
            store = itemView.findViewById(R.id.tv_store);
            num = itemView.findViewById(R.id.tv_match_num);
            time = itemView.findViewById(R.id.tv_match_time);
        }
    }
}
