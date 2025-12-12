package com.example.fitsnacks.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitsnacks.R;
import com.example.fitsnacks.data.SnackEntry;
import java.util.ArrayList;
import java.util.List;

public class SnackListAdapter extends RecyclerView.Adapter<SnackListAdapter.VH> {
    private final List<SnackEntry> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private int maxItems = Integer.MAX_VALUE; // default show all

    public interface OnItemClickListener {
        void onItemClick(SnackEntry snack);
    }

    public SnackListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Limit visible items to the most recent N (assumes list is ordered newest first).
     */
    public void setMaxItems(int max) {
        this.maxItems = max <= 0 ? Integer.MAX_VALUE : max;
    }

    /**
     * Update items using DiffUtil for efficient updates.
     */
    public void setItems(List<SnackEntry> newList) {
        if (newList == null) newList = new ArrayList<>();
        final List<SnackEntry> old = new ArrayList<>(this.items);

        // prepare the sublist view according to maxItems (newList ordered newest first)
        final List<SnackEntry> visible = new ArrayList<>();
        for (int i = 0; i < newList.size() && i < maxItems; i++) {
            visible.add(newList.get(i));
        }

        List<SnackEntry> finalNew = visible;
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return old.size(); }

            @Override
            public int getNewListSize() { return finalNew.size(); }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return old.get(oldItemPosition).id == finalNew.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                SnackEntry a = old.get(oldItemPosition);
                SnackEntry b = finalNew.get(newItemPosition);
                return a.id == b.id
                        && (a.name == null ? b.name == null : a.name.equals(b.name))
                        && a.calories == b.calories
                        && (a.portion == null ? b.portion == null : a.portion.equals(b.portion))
                        && (a.date == null ? b.date == null : a.date.equals(b.date));
            }
        });
        this.items.clear();
        this.items.addAll(finalNew);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snack, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SnackEntry s = items.get(position);
        holder.name.setText(s.name);
        holder.calories.setText(String.format(java.util.Locale.getDefault(), "%d %s", s.calories, holder.itemView.getContext().getString(R.string.label_cal)));
        holder.date.setText(s.date);
        holder.portion.setText(s.portion == null ? "" : s.portion);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(s);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, calories, date, portion;

        VH(@NonNull View itemView) {
            super(itemView);
            // use IDs defined in layout/item_snack.xml
            name = itemView.findViewById(R.id.snack_name);
            calories = itemView.findViewById(R.id.snack_calories);
            date = itemView.findViewById(R.id.snack_time);
            portion = itemView.findViewById(R.id.snack_portion);
        }
    }
}