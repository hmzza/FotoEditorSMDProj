package com.hamzaiqbal.fotoeditorsmdproj;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;




public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 101;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();//FOR ALARMED NOTIFICATION
        scheduleDailyNotification();
        Button captureButton = findViewById(R.id.uploadPhoto);
        Button galleryButton = findViewById(R.id.uploadgallery);

        captureButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                         != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(MainActivity.this,
                             new String[]{Manifest.permission.CAMERA},
                             PERMISSIONS_REQUEST_CAMERA);
                 } else {
                     try {
                         takePicture();
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 }
             }
         }
        );

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_STORAGE);
                } else {
                    selectPicture();
                }
            }
        });

    }

    /////////////////////////////////////////////
    //    CODE FOR SCHEDULED NOTIFICATIONS
    /////////////////////////////////////////////
    /////////////////////////////////////////////
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id1), getString(R.string.channel_name1), NotificationManager.IMPORTANCE_DEFAULT);

            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.horr_scr);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            channel.setSound(soundUri, audioAttributes);

            // Create the channel
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
    private void scheduleDailyNotification() {
        // Define a time for the notification (e.g., 10:00 AM)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9); // Set the hour of the day (24-hour format)
        calendar.set(Calendar.MINUTE, 12);       // Set the minute
        calendar.set(Calendar.SECOND, 30);       // Set the second to zero

        // Check if the time is past, add one day if it's past
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }


    /////////////////////////////////////////////
    //    CODE FOR uploading pic from camera
    /////////////////////////////////////////////
    /////////////////////////////////////////////

    private void takePicture() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.hamzaiqbal.fotoeditorsmdproj.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /////////////////////////////////////////////
    //    CODE FOR uploading pic from gallery
    /////////////////////////////////////////////
    /////////////////////////////////////////////
    private void selectPicture() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    takePicture();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, Editor.class);
            intent.putExtra("uri", imageUri.toString());
            startActivity(intent);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Intent intent = new Intent(this, Editor.class);
            intent.putExtra("uri", selectedImage.toString());
            startActivity(intent);
        }
    }
}