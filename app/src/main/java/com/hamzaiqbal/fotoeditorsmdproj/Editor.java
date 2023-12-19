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

import java.io.IOException;


public class Editor extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap currentBitmap; // To hold the current bitmap

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
                throw new RuntimeException(e);
            }
        }

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(-90); // Rotate left
            }
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(90); // Rotate right
            }
        });
    }

    private void rotateImage(int degrees) {
        if (currentBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(currentBitmap);
        }
    }
}
