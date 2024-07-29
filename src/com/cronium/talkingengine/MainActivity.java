package com.cronium.talkingengine;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private ImageView imageView;
    private TextView textView;
    private Uri idleImageUri;
    private ArrayList<Uri> touchImageUris = new ArrayList<>();
    private int currentTouchImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        Button selectImagesButton = findViewById(R.id.selectImagesButton);

        // Check for READ_EXTERNAL_STORAGE permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        selectImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!touchImageUris.isEmpty()) {
                        // Show the current touch image
                        imageView.setImageURI(touchImageUris.get(currentTouchImageIndex));
                        // Move to the next image
                        currentTouchImageIndex = (currentTouchImageIndex + 1) % touchImageUris.size();
                        // Revert to idle image after a short delay
                        imageView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageURI(idleImageUri);
                            }
                        }, 100); // Show touch image for 100 milliseconds
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                // Handle single image selection
                if (data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    if (idleImageUri == null) {
                        idleImageUri = selectedImageUri;
                        imageView.setImageURI(idleImageUri);
                    } else {
                        touchImageUris.add(selectedImageUri);
                    }
                }
                // Handle multiple image selection
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        if (idleImageUri == null) {
                            idleImageUri = imageUri;
                            imageView.setImageURI(idleImageUri);
                        } else {
                            touchImageUris.add(imageUri);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
                  }
