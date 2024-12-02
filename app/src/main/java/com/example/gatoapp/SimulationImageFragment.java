package com.example.gatoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;


public class SimulationImageFragment extends Fragment {

    private ImageView imageView;
    private static final String ARG_IMAGE_URI = "IMAGE_URI";

    private Uri imageUri;

    public static SimulationImageFragment newInstance(Uri uri) {
        SimulationImageFragment fragment = new SimulationImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URI, uri.toString()); // Store Uri as a String
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String uriString = getArguments().getString(ARG_IMAGE_URI);
            imageUri = Uri.parse(uriString);  // Convert back to Uri
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Find the ImageView in the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_simulation_image, container, false);
        imageView = rootView.findViewById(R.id.simulationImage);
        imageView.setRotation(90);

        // Use the Uri to load the image
        if (imageUri != null) {

            displayImage(imageUri, imageView);
        }

        return rootView;
    }
    private void displayImage(Uri uri, ImageView imageView) {
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}