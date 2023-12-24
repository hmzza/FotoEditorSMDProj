package com.hamzaiqbal.fotoeditorsmdproj;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class FilterItem {
        private GPUImageFilter filter;
        private String name;
        private int imageResId; // Resource ID for the filter preview image

        public FilterItem(GPUImageFilter filter, String name, int imageResId) {
            this.filter = filter;
            this.name = name;
            this.imageResId = imageResId;
        }

        public GPUImageFilter getFilter() {
            return filter;
        }

        public String getName() {
            return name;
        }

        public int getImageResId() {
            return imageResId;
        }


}
