package com.example.googlemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;

    Button findLocationButton;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.Search_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        findLocationButton = findViewById(R.id.findLocationButton);

        findLocationButton.setOnClickListener(v -> findMyLocation());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title(location));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }

    private void findMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(MainActivity.this, "Location providers are disabled.", Toast.LENGTH_SHORT).show();
            return;
        }

        Location location = null;
        if (isGPSEnabled) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (isNetworkEnabled && location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);


            map.addMarker(new MarkerOptions().position(latLng).title("My Location"));

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        } else {
            Toast.makeText(MainActivity.this, "Unable to retrieve current location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment = null;
    }
}
