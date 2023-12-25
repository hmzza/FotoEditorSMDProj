package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;

public class FiltersFragment extends Fragment {
    public interface FiltersFragmentListener {
        void onFilterSelected(GPUImageFilter filter);
    }

    private RecyclerView filtersRecyclerView;
    private FilterAdapter filterAdapter;
    private FiltersFragmentListener listener;

    public FiltersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_fragment_filters, container, false);

        // Set up the RecyclerView
        filtersRecyclerView = view.findViewById(R.id.filtersRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        filtersRecyclerView.setLayoutManager(layoutManager);

        // Initialize your filters list
        List<FilterItem> filterItems = new ArrayList<>();
        filterItems.add(new FilterItem(new GPUImageRGBFilter(), "None", R.drawable.fastulogo));
        filterItems.add(new FilterItem(new GPUImageSepiaToneFilter(), "Sepia", R.drawable.filter_sepia));
        filterItems.add(new FilterItem(new GPUImageContrastFilter(2.0f), "Contrast", R.drawable.filter_contrast));
        filterItems.add(new FilterItem(new GPUImageSaturationFilter(), "Saturation", R.drawable.filter_saturation));
        filterItems.add(new FilterItem(new GPUImageHueFilter(), "Hue", R.drawable.filter_hue));
        filterItems.add(new FilterItem(new GPUImageBrightnessFilter(), "Brightness", R.drawable.filter_brightness));
        filterItems.add(new FilterItem(new GPUImageMonochromeFilter(), "Mono", R.drawable.filter_mono));
        filterItems.add(new FilterItem(new GPUImageGaussianBlurFilter(), "Blur", R.drawable.filter_blur));






        // Initialize the FilterAdapter with the filters list and the listener
        filterAdapter = new FilterAdapter(filterItems, filter -> {
            // Forward the selected filter to the activity via the listener
            if (listener != null) {
                listener.onFilterSelected(filter);
            }
        });

        // Set the adapter to the RecyclerView
        filtersRecyclerView.setAdapter(filterAdapter);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure that the containing activity implements the FiltersFragmentListener
        if (context instanceof FiltersFragmentListener) {
            listener = (FiltersFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FiltersFragmentListener");
        }
    }

    // This interface callback method is triggered when a filter is clicked in the adapter
    public void onFilterClicked(GPUImageFilter filter) {
        // Forward the selected filter to the activity via the listener
        if (listener != null) {
            listener.onFilterSelected(filter);
        }
    }
}