package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.io.IOException;

public class Editor extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap currentBitmap; // To hold the current bitmap
    private static final int UCROP_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageView = findViewById(R.id.imageView);
        ImageView button_crop = findViewById(R.id.button_crop);
        ImageView button_rotate_left = findViewById(R.id.button_rotate_left);
        ImageView button_rotate_right = findViewById(R.id.button_rotate_right);

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
}
