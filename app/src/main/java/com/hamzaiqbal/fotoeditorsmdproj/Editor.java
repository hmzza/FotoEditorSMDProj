package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class Editor extends AppCompatActivity implements FiltersFragment.FiltersFragmentListener {
    private FiltersFragment filtersFragment;
    private ImageView imageView, buttonApplyFilter;
    private HorizontalScrollView horizontalScrollView;
    private Bitmap currentBitmap; // To hold the current bitmap
    private static final int UCROP_REQUEST_CODE = 3;
    private FrameLayout filtersContainer;
    private GPUImageFilter selectedFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageView = findViewById(R.id.imageView);
        ImageView button_crop = findViewById(R.id.button_crop);
        ImageView button_rotate_left = findViewById(R.id.button_rotate_left);
        ImageView button_rotate_right = findViewById(R.id.button_rotate_right);
        filtersContainer = findViewById(R.id.fragment_container);
        ImageView button_filter = findViewById(R.id.button_filter);
        horizontalScrollView = findViewById(R.id.edit_icons);
        buttonApplyFilter = findViewById(R.id.button_apply_filter);
        Intent intent = getIntent();
        if (intent.hasExtra("uri")) {
            Uri imageUri = Uri.parse(intent.getStringExtra("uri"));
            try {
                currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(currentBitmap);
            } catch (IOException e) {
                e.printStackTrace(); // Handle this properly in production code
            }
        }

        button_rotate_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(-90); // Rotate left by 90 degrees
            }
        });

        button_rotate_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(90); // Rotate right by 90 degrees
            }
        });

        button_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("uri")) {
                    Uri sourceUri = Uri.parse(intent.getStringExtra("uri"));
                    startCrop(sourceUri);
                }
            }
        });

        // Set the click listener for the filter button
        button_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the icons menu
                horizontalScrollView.setVisibility(View.GONE);
                // Show the filters fragment
                showFiltersFragment(); // This should be called to show the FiltersFragment
            }
        });
        buttonApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The filter has already been applied; now we just hide the fragment and show the icons menu
                if (filtersFragment != null) {
                    getSupportFragmentManager().beginTransaction().hide(filtersFragment).commit();
                }
                filtersContainer.setVisibility(View.GONE); // Hide the container
                horizontalScrollView.setVisibility(View.VISIBLE); // Show the icon menu
                buttonApplyFilter.setVisibility(View.GONE); // Hide the "tick" button
                // Now we save the state of the current bitmap with the applied filter
                currentBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            }
        });

    }

    private void rotateImage(int degrees) {
        if (currentBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap rotatedBitmap = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
            currentBitmap = rotatedBitmap; // Update the current bitmap to the rotated one
        }
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "SampleCropImage.jpeg"));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);

        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true); // Allow freeform crop
        options.setCircleDimmedLayer(false);
        options.setShowCropGrid(true); // Show the grid lines
        options.setHideBottomControls(false); // Show bottom controls
        // Aspect ratio options
        options.setAspectRatioOptions(0,
                new AspectRatio("Original", 1, 1),
                new AspectRatio("Square", 1, 1),
                new AspectRatio("3:4", 3, 4),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("16:9", 16, 9)
        );

        uCrop.withOptions(options);
        uCrop.start(this, UCROP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCROP_REQUEST_CODE && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    imageView.setImageBitmap(currentBitmap);
                } catch (IOException e) {
                    e.printStackTrace(); // Handle this properly in production code
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            cropError.printStackTrace(); // Handle this properly in production code
        }
    }

    private void showFiltersFragment() {
        filtersFragment = (FiltersFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (filtersFragment == null) {
            filtersFragment = new FiltersFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, filtersFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(filtersFragment)
                    .commit();
        }
        filtersContainer.setVisibility(View.VISIBLE); // Ensure the container is visible
    }
    @Override
    public void onFilterSelected(GPUImageFilter filter) {
        selectedFilter = filter; // Store the selected filter
        applyFilterToImage(); // Apply the filter immediately to the image
        // Show the "tick" button to confirm the application of the filter
        buttonApplyFilter.setVisibility(View.VISIBLE);
    }

    private void applyFilterToImage() {
        if (selectedFilter != null && currentBitmap != null) {
            GPUImage gpuImage = new GPUImage(this);
            gpuImage.setImage(currentBitmap); // Set the current image
            gpuImage.setFilter(selectedFilter); // Set the selected filter
            Bitmap filteredBitmap = gpuImage.getBitmapWithFilterApplied(); // Get the filtered bitmap
            imageView.setImageBitmap(filteredBitmap); // Update the ImageView with the filtered bitmap
            // No need to set currentBitmap here if we're not saving the state yet
        }
    }

}

    // Initialize this in your onCreate or wherever appropriate
//    private void setupApplyFilterButton() {
//        buttonApplyFilter = findViewById(R.id.button_apply_filter);
//        buttonApplyFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Hide the FiltersFragment and show the icon menu
//                if (filtersFragment != null) {
//                    getSupportFragmentManager().beginTransaction().hide(filtersFragment).commit();
//                }
//                filtersContainer.setVisibility(View.GONE); // Hide the container
//                horizontalScrollView.setVisibility(View.VISIBLE); // Show the icon menu
//                // The filter has already been applied, just hide the "tick" button
//                buttonApplyFilter.setVisibility(View.GONE);
//            }
//        });
//    }
//}
//    @Override
//    public void onFilterSelected(GPUImageFilter filter) {
//        selectedFilter = filter; // Store the selected filter
//        applyFilterToImage(); // Apply the filter to the image
//        // Hide the FiltersFragment
//        if (filtersFragment != null) {
//            getSupportFragmentManager().beginTransaction()
//                    .hide(filtersFragment)
//                    .commit();
//            filtersContainer.setVisibility(View.GONE); // Hide the container
//        }
//    }

//    private void applyFilterToImage() {
//        if (currentBitmap != null && selectedFilter != null) {
//            GPUImage gpuImage = new GPUImage(this);
//            gpuImage.setImage(currentBitmap);
//            gpuImage.setFilter(selectedFilter);
//            Bitmap filteredBitmap = gpuImage.getBitmapWithFilterApplied();
//            imageView.setImageBitmap(filteredBitmap);
//        }
//    }
//}
