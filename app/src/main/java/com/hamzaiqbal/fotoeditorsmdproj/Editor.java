package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.yalantis.ucrop.UCrop;

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
        Button rotateLeftButton = findViewById(R.id.button_rotate_left);
        Button rotateRightButton = findViewById(R.id.button_rotate_right);

        Intent intent = getIntent();
        if (intent.hasExtra("uri")) {
            Uri imageUri = Uri.parse(intent.getStringExtra("uri"));
            imageView.setImageURI(imageUri);
            // Load the bitmap from the URI for manipulation
            try {
                currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace(); // Handle this properly in production code
            }
        }

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(-90); // Rotate left by 90 degrees
            }
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(90); // Rotate right by 90 degrees
            }
        });

        findViewById(R.id.button_crop).setOnClickListener(new View.OnClickListener() {
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
        uCrop.withAspectRatio(16, 9)
                .withMaxResultSize(800, 800)
                .start(this, UCROP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCROP_REQUEST_CODE && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageView.setImageURI(resultUri);
                // Load the new cropped bitmap
                try {
                    currentBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
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
