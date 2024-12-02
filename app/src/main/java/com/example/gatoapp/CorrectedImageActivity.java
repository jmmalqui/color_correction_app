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
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.io.InputStream;


public class CorrectedImageActivity extends AppCompatActivity {
    private Button gobackButton;
    private TabLayout tabs;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.corrected_image);

        String originalUriString = getIntent().getStringExtra("originalImageUri");
        String simulatedUriString = getIntent().getStringExtra("simulatedImageUri");
        String correctedUriString = getIntent().getStringExtra("correctedImageUri");
        String correctedsimUriString = getIntent().getStringExtra("correctedSimulationImageUri");

        tabs = findViewById(R.id.optionsTabs);
        pager = findViewById(R.id.viewpager);

        tabs.setupWithViewPager(pager);

        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        vpAdapter.addFragment(OriginalImageFragment.newInstance(Uri.parse(originalUriString)), "Original");
        vpAdapter.addFragment(CorrectedImageFragment.newInstance(Uri.parse(correctedUriString)), "Corrected");
        vpAdapter.addFragment(SimulationImageFragment.newInstance(Uri.parse(simulatedUriString)), "Simulation");
        vpAdapter.addFragment(CorrectedSimulationImageFragment.newInstance(Uri.parse(correctedsimUriString)), "CorrectedSimulation");

        pager.setAdapter(vpAdapter);

//        gobackButton = findViewById(R.id.menubtn);
//        gobackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goBack();
//            }
//        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.corrected), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

//    private void goBack() {
//        Intent intent = new Intent(CorrectedImageActivity.this, MainActivity.class);
//        startActivity(intent);
//    }
}