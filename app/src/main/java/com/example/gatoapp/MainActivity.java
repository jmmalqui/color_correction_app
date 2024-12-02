package com.example.gatoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ImageView originalImage; // may use later
    private Button takePhotoButton;
    private Spinner defficiencySelectSpinner;
    private SeekBar severitySeekBar;
    private TextView severityText;
    private Python py;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        defficiencySelectSpinner = (Spinner) findViewById(R.id.deficiency_select_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.deficiency_type,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        defficiencySelectSpinner.setAdapter(adapter);
        severityText = findViewById(R.id.severityText);
        severitySeekBar = findViewById(R.id.severityBar);
        severitySeekBar.setMax(100);

        severitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                severityText.setText(getSeverity(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        takePhotoButton = findViewById(R.id.btnTomarFoto);

        py = Python.getInstance();
        CheckCameraPermissions();

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoFromCamera();
            }
        });
        SetSystem();
    }

    private void SetSystem() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void CheckCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void takePhotoFromCamera() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "WTF", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri saveByteArrayToFile(String fileName, byte[] byteArray) {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(this, "com.example.gatoapp.fileprovider", file);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            assert data != null;
            Bundle extras = data.getExtras();
            assert extras != null;
            Bitmap BMP_Photo = (Bitmap) extras.get("data");
            File tempFile = null;
            try {
                tempFile = File.createTempFile("temp_image", ".png", getCacheDir());
                tempFile.deleteOnExit();
                try (OutputStream out = new FileOutputStream(tempFile)) {
                    assert BMP_Photo != null;
                    BMP_Photo.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!Python.isStarted()) {
                Context ctx = getApplicationContext();
                Python.start(new AndroidPlatform(this));
            }
            String deficiencyType = defficiencySelectSpinner.getSelectedItem().toString();
            String severity = getSeverity(severitySeekBar.getProgress());
//
//            assert tempFile != null;
            String filePath = tempFile.getAbsolutePath();
            List<PyObject> result = py.getModule("main").callAttr("corrected_sim", deficiencyType, severity, filePath).asList();

            byte[] original = result.get(0).toJava(byte[].class);
            byte[] simulated = result.get(1).toJava(byte[].class);
            byte[] corrected = result.get(2).toJava(byte[].class);
            byte[] simulation = result.get(3).toJava(byte[].class);

            Uri originalUri = saveByteArrayToFile("original_image.jpg", original);
            Uri simulatedUri = saveByteArrayToFile("simulated_image.jpg", simulated);
            Uri correctedUri = saveByteArrayToFile("corrected_image.jpg", corrected);
            Uri correctedSimulationUri = saveByteArrayToFile("corrected_simulation_image.jpg", simulation);


            Intent intent = new Intent(MainActivity.this, CorrectedImageActivity.class);
            intent.putExtra("originalImageUri", originalUri.toString());
            intent.putExtra("simulatedImageUri", simulatedUri.toString());
            intent.putExtra("correctedImageUri", correctedUri.toString());
            intent.putExtra("correctedSimulationImageUri", correctedSimulationUri.toString());

            startActivity(intent);
        }
    }

    @NonNull
    private String getSeverity(int value) {
        return String.valueOf((float) ((float)value/ 100.0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}