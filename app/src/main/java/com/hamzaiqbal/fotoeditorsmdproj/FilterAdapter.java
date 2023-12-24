package com.hamzaiqbal.fotoeditorsmdproj;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
interface FilterClickListener {
    void onFilterClicked(GPUImageFilter filter);
}

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
    private List<FilterItem> filterItems;
//    private List<GPUImageFilter> filtersList; // Your list of filter instances
    private FilterClickListener listener; // Listener to communicate back to the fragment/activity

    // Adapter's constructor
    public FilterAdapter(List<FilterItem> filterItems, FilterClickListener listener) {
        this.filterItems = filterItems;
        this.listener = listener;
    }
    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_item, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        FilterItem item = filterItems.get(position);
        holder.filterImage.setImageResource(item.getImageResId()); // Set the preview image
        holder.filterName.setText(item.getName()); // Set the filter name
        
        // Assign the click listener to each filter item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterClicked(item.getFilter());
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterItems.size();
    }

    // Method to set the click listener
    public void setFilterClickListener(FilterClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class for the RecyclerView items
    static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView filterName;
        ImageView filterImage;

        FilterViewHolder(View itemView) {
            super(itemView);
            filterImage = itemView.findViewById(R.id.filterImage);
            filterName = itemView.findViewById(R.id.filterName);        }
    }
}