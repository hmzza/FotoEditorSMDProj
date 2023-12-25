package com.hamzaiqbal.fotoeditorsmdproj;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class DoodleFragment extends Fragment {

    private DoodleView doodleView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doodle, container, false);
        doodleView = view.findViewById(R.id.doodle_view);
        return view;
    }

    public Bitmap getDoodleBitmap() {
        if (doodleView != null) {
            return doodleView.getBitmap();
        } else {
            return null;
        }
    }
}
