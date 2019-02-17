package com.example.rshuttle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private boolean already_Ran = false;
    private int lastTime = 0;
    private List<Marker> markers;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public static int MY_LOCATION_REQUEST_CODE = 99;



    //Initialization
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocationUpdates();
        getLastLocation();

        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyAm7IpcksT6ulzv2GfZuYaR954-Pcfztsw");

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);


    }


    //Setting up the map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if(checkLocationPermission()){
            mMap.setMyLocationEnabled(true);

        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        createSearch();
/*
        if(!already_Ran) {
            try {
                setBusStops(mMap);
                already_Ran = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        if(lastTime == 0) {
            try {
                markers = setLiveBus(mMap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastTime++;
        if(lastTime > 3000) {
            lastTime = 0;
            for(Marker marker: markers) {
                marker.remove();
            }
        }


    }

    //Gives me the current Location when you press yourself
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    //Notifies me that I've pressed the MyLocation button which zooms me into my location
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    //Check Permissions for Location
    /*public boolean checkLocationPermission(){
        //If permission is granted, set the location to be enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return true;
        } else {
            //else show an alert that permissions aren't enabled and the app just won't show your location

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.create();
            dialog.setMessage("Please Enable Location Permissions");
            dialog.setTitle("Permissions");


            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
            return false;

        }
    }*/

    /***
     * This class is to be used in co-junction with setBusStop to place stop markers
     *
     * @author Justin Yau, Jeffrey Weng
     */
    private class stopRunnable implements Runnable {

        private GoogleMap map;
        private Map<String, String[]> stops;

        public stopRunnable(GoogleMap map) {
            this.map = map;
        }
        public void run() {
            try {
                RealTime time = new RealTime();
                this.stops = time.stops("643");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Map<String, String[]> getStops() {
            return this.stops;
        }

    }

    /***
     * This class is meant to be used in co-junction with setLiveBus to update live bus markers
     *
     * @author Justin Yau
     */
    private class liveBus implements Runnable {
        private GoogleMap map;
        private Map<String, float[][]> buses;
        private Map<String, List<String>> routes;

        public liveBus(GoogleMap map) {
            this.map = map;
        }
        public void run() {
            try {
                RealTime time = new RealTime();
                this.routes = time.routes("643");
                this.buses = new HashMap<>();
                for(String key: this.routes.keySet()) {
                    System.out.println(key);
                    float[][] buses = time.busLocations("643", key);
                    if(buses != null) {
                        this.buses.put(key, time.busLocations("643", key));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Map<String, float[][]> getBuses() {
            return this.buses;
        }

        public Map<String, List<String>> getRoutes() {
            return this.routes;
        }
    }

    /***
     * This method retrieves and creates markers for all the bus stops
     *
     * @param map - The map
     * @throws InterruptedException
     * @author Justin Yau, Jeffrey Weng
     */
    public void setBusStops(GoogleMap map) throws InterruptedException{
        stopRunnable bees = new stopRunnable(map);
        Thread thread = new Thread(bees);
        thread.start();
        thread.join();
        Map<String, String[]> stops = bees.getStops();
        for(String key: stops.keySet()) {
            String[] info = stops.get(key);
            System.out.println("Stop ID: " + key + " Name: " + info[0] + " Latitude: " + info[1] + " Longitude: " + info[2]);
            System.out.println((Double.parseDouble(info[1]) + " " + Double.parseDouble(info[2])));
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(info[1]), Double.parseDouble(info[2])))
                    .title(info[0]));
        }
    }

    /***
     * This method updates the live bus and markers
     *
     * @param map - The map
     * @return - A list of markers set representing the live bus
     * @throws InterruptedException
     * @author Justin Yau
     */
    public List<Marker> setLiveBus(GoogleMap map) throws InterruptedException{
        liveBus run = new liveBus(map);
        Thread thread = new Thread(run);
        thread.start();
        thread.join();
        Map<String, float[][]> buses = run.getBuses();
        Map<String, List<String>> routes = run.getRoutes();
        List<Marker> markers = new ArrayList<>();
        for(String key: buses.keySet()) {
            if(buses.get(key) != null) {
                for(float[] bus: buses.get(key)) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(bus[0], bus[1]))
                            .title(routes.get(key).get(0));
                    markers.add(map.addMarker(marker));
                }
            }
        }
        return markers;
    }

    public void createSearch(){

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Maps", "Place: " + place.getName() + ", " + place.getId());
                Log.i("Maps", "Place: " + place.getName() + ", " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Maps", "An error occurred: " + status);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_LOCATION_REQUEST_CODE);
    }



    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (checkLocationPermission()) {
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());

            /*
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        RealTime time = new RealTime();
                        Map<String, List<String>> routes = time.routes("643");
                        for(String key: routes.keySet()) {
                            for(String info: routes.get(key)) {
                                System.out.println("Route ID: " + key + "Stop: " + info);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }); */

            /*
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        RealTime time = new RealTime();
                        Map<String, String> times = time.timeAtStop("643", "4224622");
                        for(String key: times.keySet()) {
                            System.out.println("Route ID: " + key + " Time of Arrival: " + times.get(key));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });*/
            /*
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        RealTime time = new RealTime();
                        Map<String, String> agencies = time.getAgencyIds("43.06354", "-77.72364", "43.09962", "-77.63356");
                        for(String key: agencies.keySet()) {
                            System.out.println("Name: " + key + " Agency Id:" + agencies.get(key));
                        }
                    } catch (Exception e) {

                    }
                }

            }); */
            // thread.start();
        }
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        if(checkLocationPermission()) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                goToLocation(location);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void goToLocation(Location location){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

}