package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;


public class Editor extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor); // Make sure you have a layout named activity_editor

        imageView = findViewById(R.id.imageView); // Make sure you have an ImageView with this id in activity_editor.xml

        Intent intent = getIntent();
        if (intent.hasExtra("bitmap")) {
            Bitmap bitmap = intent.getParcelableExtra("bitmap");
            imageView.setImageBitmap(bitmap);
        } else if (intent.hasExtra("uri")) {
            Uri imageUri = Uri.parse(intent.getStringExtra("uri"));
            imageView.setImageURI(imageUri);
        }
    }
}
