package com.hamzaiqbal.fotoeditorsmdproj;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class Editor extends AppCompatActivity implements FiltersFragment.FiltersFragmentListener {
    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private DoodleView doodleView;
    private ImageView buttonDoneDoodle;
    private LinearLayout colorPalette;
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
        createNotificationChannel();
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

        /////////////////////////////////////////////
        //        CODE FOR CROP AND ROTATE
        /////////////////////////////////////////////
        /////////////////////////////////////////////
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

        /////////////////////////////////////////////
        //            CODE FOR FILTERS
        /////////////////////////////////////////////
        /////////////////////////////////////////////
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
                currentBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            }
        });

        /////////////////////////////////////////////
        //            CODE FOR DOODLE
        /////////////////////////////////////////////
        /////////////////////////////////////////////

        doodleView = findViewById(R.id.doodle_view);

        ImageView btnDoodle = findViewById(R.id.btn_doodle);
        buttonDoneDoodle = findViewById(R.id.button_done_doodle);
        btnDoodle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doodleView.setVisibility(View.VISIBLE);
                colorPalette.setVisibility(View.VISIBLE);
                buttonDoneDoodle.setVisibility(View.VISIBLE); // Show the done button
                horizontalScrollView.setVisibility(View.GONE);
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        adjustDoodleViewSize();
                    }
                });
            }
        });

        // Set up the done button for doodle
        buttonDoneDoodle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap doodleBitmap = doodleView.getDoodleBitmap();
                // Combine it with the current image bitmap
                Bitmap combinedBitmap = combineImages(currentBitmap, doodleBitmap);
                // Set the combined bitmap as the new image
                imageView.setImageBitmap(combinedBitmap);

                // Update the current bitmap to the combined one
                currentBitmap = combinedBitmap;

                doodleView.setVisibility(View.GONE); // Hide the DoodleView
                colorPalette.setVisibility(View.GONE); // Hide the color palette
                buttonDoneDoodle.setVisibility(View.GONE); // Hide the done button
                horizontalScrollView.setVisibility(View.VISIBLE);
            }
        });

        colorPalette = findViewById(R.id.color_palette);

        // Color palette setup
        findViewById(R.id.color_black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doodleView.setColor(getResources().getColor(R.color.black));
            }
        });
        findViewById(R.id.color_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doodleView.setColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        findViewById(R.id.color_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doodleView.setColor(getResources().getColor(R.color.blue));
            }
        });

        findViewById(R.id.color_yellow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doodleView.setColor(getResources().getColor(R.color.yellow));
            }
        });

        findViewById(R.id.color_green).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doodleView.setColor(getResources().getColor(R.color.green));
            }
        });

        /////////////////////////////////////////////
        //        CODE FOR DOWNLOAD
        /////////////////////////////////////////////
        /////////////////////////////////////////////


        ImageView btnSave = findViewById(R.id.downlaod);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10 and above uses scoped storage and no need for WRITE_EXTERNAL_STORAGE permission
                    saveImageToGallery(currentBitmap);
                } else {
                    // For older versions, check if the WRITE_EXTERNAL_STORAGE permission is granted
                    if (ContextCompat.checkSelfPermission(Editor.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // If not, request the permission
                        ActivityCompat.requestPermissions(Editor.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_STORAGE);
                    } else {
                        // Permission has already been granted, save the image
                        saveImageToGallery(currentBitmap);
                    }
                }
            }
        });


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

    /////////////////////////////////////////////
    //        CODE FOR CROP AND ROTATE
    /////////////////////////////////////////////
    /////////////////////////////////////////////

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


    /////////////////////////////////////////////
    //            CODE FOR FILTERS
    /////////////////////////////////////////////
    /////////////////////////////////////////////
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

    /////////////////////////////////////////////
    //            CODE FOR DOODLE
    /////////////////////////////////////////////
    /////////////////////////////////////////////

    // Utility method to combine two bitmaps
    private Bitmap combineImages(Bitmap backgroundBitmap, Bitmap overlayBitmap) {
        // Create a new bitmap that's the same size as the background
        Bitmap combined = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Create a new canvas to draw on the combined bitmap
        Canvas canvas = new Canvas(combined);

        // Draw the background image first
        canvas.drawBitmap(backgroundBitmap, new Matrix(), null);

        // Calculate the scale factor between the doodle bitmap and the background bitmap
        float scaleX = (float) backgroundBitmap.getWidth() / overlayBitmap.getWidth();
        float scaleY = (float) backgroundBitmap.getHeight() / overlayBitmap.getHeight();

        // Create a matrix for scaling
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY);

        // Draw the overlay bitmap (doodle) on top of the background, using the scale matrix
        canvas.drawBitmap(overlayBitmap, scaleMatrix, null);
        currentBitmap = combined;
//        imageView.setImageBitmap(currentBitmap);

        // Return the combined bitmap
        return combined;
    }
    private void adjustDoodleViewSize() {
        // Get the layout parameters of the ImageView
        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

        // Create new layout parameters for DoodleView based on ImageView's params
        RelativeLayout.LayoutParams doodleParams = new RelativeLayout.LayoutParams(imageParams.width, imageParams.height);

        // Align DoodleView to the exact position of ImageView
        doodleParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView);
        doodleParams.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView);
        doodleParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.imageView);
        doodleParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imageView);

        // Apply the layout parameters to DoodleView
        doodleView.setLayoutParams(doodleParams);
    }

    /////////////////////////////////////////////
    //         CODE FOR SAVING IMAGE
    /////////////////////////////////////////////
    /////////////////////////////////////////////


    private void saveImageToGallery(Bitmap finalBitmap) {
        // Get the external storage directory
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        // Create a directory for your app's saved images
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // Name the file with the current timestamp to avoid duplicates
        String fileName = "Image_" + System.currentTimeMillis() + ".jpg";
        File file = new File(myDir, fileName);

        // Save the image
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            // Inform the media scanner about the new file so that it is immediately available to the user
            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);

                            // Run on UI thread to show a toast notification
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Editor.this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                                }
                            });

                            sendNotification(fileName, uri);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            // Run on UI thread to show a toast notification for the error
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Editor.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /////////////////////////////////////////////
    //         CODE FOR REQUESTS
    /////////////////////////////////////////////
    /////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with saving the image
                saveImageToGallery(currentBitmap);
            } else {
                // Permission was denied, show a message to the user
                Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /////////////////////////////////////////////
    //         CODE FOR PUSH NOTIFICATION
    /////////////////////////////////////////////
    /////////////////////////////////////////////

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String fileName, Uri fileUri) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "image/jpeg");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setSmallIcon(R.drawable.logo_with_black) // Replace with your own drawable icon
                .setContentTitle("WOHOO!! Image Saved")
                .setContentText("Tap to view the image.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }



}
