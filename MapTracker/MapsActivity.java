package cz.ujep.ki.mapy2022;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import cz.ujep.ki.mapy2022.databinding.ActivityMapsBinding;

enum ServiceState {
    ON,
    OFF,
    PENDING
}

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap = null;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private DrawLocationCallback locationCallback;
    private final int LOC_REQ = 1;
    private ServiceState gpsState = ServiceState.OFF;
    private boolean elevationServiceOn = false;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        buildLocationRequest();

        locationCallback = new DrawLocationCallback(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpsState == ServiceState.OFF) {
                    gpsState = ServiceState.PENDING;
                    startLocationServiceWithPermissionsCheck();
                }
            }
        });
        Log.d("gui thread", new Long(Thread.currentThread().getId()).toString());
    }

    void startLocationServiceWithPermissionsCheck() {
        if (
                ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                  Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOC_REQ);
        }
        else {
            startLocationService();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        switch (requestCode) {
            case LOC_REQ:
                if(grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    startLocationService();
                else {
                    Toast.makeText(this, "No permission, no map positions", Toast.LENGTH_SHORT).show();
                    gpsState =ServiceState.OFF;
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressLint("MissingPermission")
    void startLocationService() {
        Task<Void> task = fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                MapsActivity.this.gpsState = ServiceState.ON;
                Toast.makeText(MapsActivity.this, "Tracker is tracking", Toast.LENGTH_SHORT).show();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, "Tracker is out of order", Toast.LENGTH_SHORT).show();
                MapsActivity.this.gpsState = ServiceState.OFF;
            }
        });
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(10);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng usti = new LatLng(50.6,14.03);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usti, 10));
    }

    void export() {
        String context = null;
        try {
            context = locationCallback.export();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid GPX writer", Toast.LENGTH_SHORT).show();
        }
        Intent sendIntent = new Intent();
        Intent intent = sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, context);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}