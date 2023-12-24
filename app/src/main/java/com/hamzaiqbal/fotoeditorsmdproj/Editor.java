package com.hamzaiqbal.fotoeditorsmdproj;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
    private ImageView emoji;
    private TextView btnSaveChanges; // Add this variable
    private ImageView imageView;
    private Bitmap originalBitmap, currentBitmap;

    private Paint currentPaint;
    private static final int UCROP_REQUEST_CODE = 3;
    private FrameLayout filtersContainer;
    private GPUImageFilter selectedFilter;
    private String selectedEmoji;

    // Doodle related variables
    private ImageView btnDoodle;
    // Doodle related variables
    // Doodle related variables
    private boolean isDoodling = false;
    private float startX, startY;
    private static final float TOUCH_TOLERANCE = 4;
    private Paint doodlePaint;
    private Path doodlePath;
    private Bitmap doodleBitmap;




    private HorizontalScrollView horizontalScrollView;
    private Bitmap currentBitmap; // To hold the current bitmap





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageView = findViewById(R.id.imageView);
        ImageView button_crop = findViewById(R.id.button_crop);
        ImageView button_rotate_left = findViewById(R.id.button_rotate_left);
        ImageView button_add_text = findViewById(R.id.button_add_text);
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
        ImageView btEmoji = findViewById(R.id.bt_emoji);


        // Set OnClickListener for the bt_emoji ImageView
        btEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the emoji selection dialog
                showEmojiDialog();
            }
        });
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

        button_add_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTextDialog();
            }
        });



    }

    private void showAddTextDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add Text")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = input.getText().toString();
                        drawTextOnBitmap(text);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void drawTextOnBitmap(String text) {
        if (currentBitmap != null) {
            Bitmap newBitmap = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(currentBitmap, 0, 0, null);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE); // Text color
            paint.setTextSize(50); // Text size
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);

            // TODO: Allow the user to choose the position of the text or implement a dragging feature
            canvas.drawText(text, 100, 100, paint); // You need to choose the x, y positions

            imageView.setImageBitmap(newBitmap);
            currentBitmap = newBitmap; // Update the current bitmap
        }
    }

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



        btnDoodle = findViewById(R.id.btn_doodle);

        doodlePaint = new Paint();
        doodlePaint.setAntiAlias(true);
        doodlePaint.setDither(true);
        doodlePaint.setColor(Color.BLACK);
        doodlePaint.setStyle(Paint.Style.STROKE);
        doodlePaint.setStrokeJoin(Paint.Join.ROUND);
        doodlePaint.setStrokeCap(Paint.Cap.ROUND);
        doodlePaint.setStrokeWidth(20);

        btnDoodle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle doodle mode
                isDoodling = !isDoodling;
                if (isDoodling) {
                    startDoodling();
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDoodling) {
                    float x = event.getX();
                    float y = event.getY();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startDoodlePath(x, y);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            doDoodlePath(x, y);
                            break;
                        case MotionEvent.ACTION_UP:
                            endDoodlePath();
                            break;
                    }
                    return true;
                }
                return false;
            }
        });

        btnSaveChanges = findViewById(R.id.btn_save_changes); // Initialize the Save button
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(); // Call the method to save changes
            }
        });
    }

    private void saveChanges() {
        // Check if an emoji or doodle is applied to the image
        if (selectedEmoji != null) {
            applyEmojiToImage(selectedEmoji); // Apply the selected emoji
        }
        if (isDoodling) {
            // Save the doodle on the image
            // For example:
            drawDoodlePath();
        }

        // Once the changes are applied, hide the doodle mode and selected emoji
        isDoodling = false;
        selectedEmoji = null;
    }

    private void startDoodling() {
        imageView.setDrawingCacheEnabled(true);
        doodleBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(doodleBitmap);
    }

    private void startDoodlePath(float x, float y) {
        doodlePath = new Path();
        doodlePath.moveTo(x, y);
        startX = x;
        startY = y;
    }

    private void doDoodlePath(float x, float y) {
        float dx = Math.abs(x - startX);
        float dy = Math.abs(y - startY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            doodlePath.quadTo(startX, startY, (x + startX) / 2, (y + startY) / 2);
            startX = x;
            startY = y;
            drawDoodlePath();
        }
    }

    private void endDoodlePath() {
        doodlePath.lineTo(startX, startY);
        drawDoodlePath();
        doodlePath.reset();
    }

    private void drawDoodlePath() {
        Canvas canvas = new Canvas(doodleBitmap);
        canvas.drawPath(doodlePath, doodlePaint);
        imageView.setImageBitmap(doodleBitmap);
    }











    public void showEmojiDialog() {
        final Dialog emojiDialog = new Dialog(this);
        emojiDialog.setContentView(R.layout.dialog_emoji_selection);

        GridView gridViewEmojis = emojiDialog.findViewById(R.id.gridViewEmojis);

            String[] emojis = {
                    "\uD83D\uDE00", // Grinning face
                    "\uD83D\uDE01", // Grinning face with smiling eyes
                    "\uD83D\uDE02", // Face with tears of joy
                    "\uD83D\uDE03", // Smiling face with open mouth
                    "\uD83D\uDE04", // Smiling face with open mouth and smiling eyes
                    "\uD83D\uDE05", // Smiling face with open mouth and cold sweat
                    "\uD83D\uDE06", // Smiling face with open mouth and closed eyes
                    "\uD83D\uDE07", // Smiling face with halo
                    "\uD83D\uDE08", // Smiling face with horns
                    "\uD83D\uDE09", // Winking face
                    "\uD83D\uDE0A", // Smiling face with smiling eyes
                    "\uD83D\uDE0B", // Face savoring food
                    "\uD83D\uDE0C", // Relieved face
                    "\uD83D\uDE0D", // Smiling face with heart-shaped eyes
                    "\uD83D\uDE0E", // Smiling face with sunglasses
                    "\uD83D\uDE0F", // Smirking face
                    "\uD83D\uDE10", // Neutral face
                    "\uD83D\uDE11", // Expressionless face
                    "\uD83D\uDE12", // Unamused face
                    "\uD83D\uDE13", // Face with cold sweat
                    "\uD83D\uDE14", // Pensive face
                    "\uD83D\uDE15", // Confused face
                    "\uD83D\uDE16", // Confounded face
                    "\uD83D\uDE17", // Kissing face
                    "\uD83D\uDE18", // Face throwing a kiss
                    "\uD83D\uDE19", // Kissing face with smiling eyes
                    "\uD83D\uDE1A", // Kissing face with closed eyes
                    "\uD83D\uDE1B", // Face with stuck-out tongue
                    "\uD83D\uDE1C", // Face with stuck-out tongue and winking eye
                    "\uD83D\uDE1D", // Face with stuck-out tongue and tightly-closed eyes
                    "\uD83D\uDE1E", // Disappointed face
                    "\uD83D\uDE1F", // Worried face
                    "\uD83D\uDE20", // Angry face
                    "\uD83D\uDE21", // Pouting face
                    "\uD83D\uDE22", // Crying face
                    "\uD83D\uDE23", // Persevering face
                    "\uD83D\uDE24", // Face with look of triumph
                    "\uD83D\uDE25", // Disappointed but relieved face
                    "\uD83D\uDE26", // Frowning face with open mouth
                    "\uD83D\uDE27", // Anguished face
                    "\uD83D\uDE28", // Fearful face
                    "\uD83D\uDE29", // Weary face
                    "\uD83D\uDE2A", // Sleepy face
                    "\uD83D\uDE2B", // Tired face
                    "\uD83D\uDE2C", // Grimacing face
                    "\uD83D\uDE2D", // Loudly crying face
                    "\uD83D\uDE2E", // Face with open mouth
                    "\uD83D\uDE2F", // Hushed face
                    "\uD83D\uDE30", // Face with open mouth and cold sweat
                    "\uD83D\uDE31", // Face screaming in fear
                    "\uD83D\uDE32",  // Astonished face
                    "\uD83D\uDE33", // Flushed face
                    "\uD83D\uDE34", // Slightly smiling face
                    "\uD83D\uDE35", // Upside-down face
                    "\uD83D\uDE36", // Winking face with tongue
                    "\uD83D\uDE37", // Squinting face with tongue
                    "\uD83D\uDE38", // Money-mouth face
                    "\uD83D\uDE39", // Hugging face
                    "\uD83D\uDE3A", // Face with hand over mouth
                    "\uD83D\uDE3B", // Shushing face
                    "\uD83D\uDE3C",  // Thinking face
                    "\uD83D\uDE3D", // Lying face
                    "\uD83D\uDE3E", // Shushing face with index finger
                    "\uD83D\uDE3F", // Face with raised eyebrow
                    "\uD83D\uDE40", // Neutral face with raised eyebrow
                    "\uD83D\uDE41", // Hushed face with raised eyebrow
                    "\uD83D\uDE42", // Frowning face with raised eyebrow
                    "\uD83D\uDE43", // Angry face with horns
                    "\uD83D\uDE44", // Pouting face with raised eyebrow
                    "\uD83D\uDE45", // Face with medical mask
                    "\uD83D\uDE46",  // Face with thermometer
                    "\uD83D\uDC3A", // Panda face
                    "\uD83D\uDC3B", // Penguin
                    "\uD83D\uDC3C", // Fish
                    "\uD83D\uDC3D", // Tropical fish
                    "\uD83D\uDC3E", // Blowfish
                    "\uD83D\uDC3F", // Dolphin
                    "\uD83D\uDC40", // Spouting whale
                    "\uD83D\uDC41", // Whale
                    "\uD83D\uDC42", // Squid
                    "\uD83D\uDC43", // Snail
                    "\uD83C\uDF45", // Tangerine
                    "\uD83C\uDF46", // Lemon
                    "\uD83C\uDF47", // Banana
                    "\uD83C\uDF48", // Pineapple
                    "\uD83C\uDF49", // Red apple
                    "\uD83C\uDF4A", // Green apple
                    "\uD83C\uDF4B", // Pear
                    "\uD83C\uDF4C", // Peach
                    "\uD83C\uDF4D", // Cherries
                    "\uD83C\uDF4E", // Strawberry
                    "\uD83C\uDF4F", // Hamburger
                    "\uD83C\uDF50", // Closed umbrella
                    "\uD83C\uDF51", // Umbrella with rain drops
                    "\uD83C\uDF52", // Umbrella on ground
                    "\uD83C\uDF53", // High voltage sign
                    "\uD83C\uDF54", // Thermometer
                    "\uD83C\uDF55", // Black scissors
                    "\uD83C\uDF56", // White scissors
                    "\uD83C\uDF57", // Mantelpiece clock
                    "\uD83C\uDF58", // Black skull and crossbones
                    "\uD83C\uDF59", // No entry
                    "\uD83C\uDF5A", // Right arrow curving left
                    "\uD83C\uDF5B", // Left arrow curving right
                    "\uD83C\uDF5C"  // Watch
            };


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                emojis
        );

        gridViewEmojis.setAdapter(adapter);

        gridViewEmojis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedEmoji = (String) parent.getItemAtPosition(position);
                applyEmojiToImage(selectedEmoji);
                emojiDialog.dismiss();
            }
        });

        emojiDialog.show();
    }
    // Apply emoji to the image
    private void applyEmojiToImage(String emoji) {
        selectedEmoji = emoji; // Store the selected emoji for later use

        if (currentBitmap != null && selectedEmoji != null) {
            // Convert the emoji string to a Bitmap (use your method for converting text to Bitmap)
            Bitmap emojiBitmap = textToBitmap(selectedEmoji);

            // Create a mutable copy of the current bitmap to draw the emoji on it
            Bitmap mutableBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            // Calculate the position to place the emoji (center of the image)
            int imageWidth = mutableBitmap.getWidth();
            int imageHeight = mutableBitmap.getHeight();

            int emojiWidth = emojiBitmap.getWidth();
            int emojiHeight = emojiBitmap.getHeight();

            // Define a scaling factor to increase the size of the emoji (adjust the value as needed)
            float scaleFactor = 5.5f;

            int scaledEmojiWidth = (int) (emojiWidth * scaleFactor);
            int scaledEmojiHeight = (int) (emojiHeight * scaleFactor);

            int xPos = (imageWidth - scaledEmojiWidth) / 2; // X position to center the emoji
            int yPos = (imageHeight - scaledEmojiHeight) / 2; // Y position to center the emoji

            // Scale the emoji bitmap
            Bitmap scaledEmojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, scaledEmojiWidth, scaledEmojiHeight, true);

            // Draw the scaled emoji bitmap on the canvas at the calculated centered position
            canvas.drawBitmap(scaledEmojiBitmap, xPos, yPos, null);

            // Update the ImageView with the modified bitmap
            imageView.setImageBitmap(mutableBitmap);
        }
    }











    // Method to convert text to Bitmap (for emoji)
    private Bitmap textToBitmap(String text) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(100); // Set the text size as needed
        paint.setColor(Color.BLACK); // Set the text color

        // Measure the text size to create a bitmap of appropriate dimensions
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 0, bounds.height(), paint);

        return bitmap;
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
