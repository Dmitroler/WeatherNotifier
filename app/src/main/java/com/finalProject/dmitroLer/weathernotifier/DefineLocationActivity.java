package com.finalProject.dmitroLer.weathernotifier;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DefineLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location location;
    private Location newLocation;
    private Address currentAddress;
    private Marker marker;
    private Address searchAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.finalProject.dmitroLer.weathernotifier.R.layout.activity_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.finalProject.dmitroLer.weathernotifier.R.id.map);
        mapFragment.getMapAsync(this);
        location = getIntent().getParcelableExtra("Location");
        currentAddress = getIntent().getParcelableExtra("Address");

        EditText searchBox = (EditText) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.search_box);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);

        findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.add_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constants.NEW_LOCATION, newLocation);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        final Context context = this;
        findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSearch = ((EditText) findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.search_box)).getText().toString();
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(toSearch, 1);
                    if (!addresses.isEmpty()) {
                        searchAddress = addresses.get(0);
                        LatLng latLng = new LatLng(searchAddress.getLatitude(), searchAddress.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        if (marker != null) marker.remove();
                        marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(searchAddress.getLocality()));
                        newLocation = new Location("");
                        newLocation.setLatitude(searchAddress.getLatitude());
                        newLocation.setLongitude(searchAddress.getLongitude());
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    } else {
                        Toast toast = new Toast(context);
                        toast.makeText(context, "No address found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            String city = currentAddress.getLocality();
            mMap.addMarker(new MarkerOptions().position(currentLocation).title(city));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
        }
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker != null) marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("New location"));

                newLocation = new Location("");
                newLocation.setLatitude(latLng.latitude);
                newLocation.setLongitude(latLng.longitude);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

}
