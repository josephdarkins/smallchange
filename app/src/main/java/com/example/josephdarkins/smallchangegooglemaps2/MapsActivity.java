package com.example.josephdarkins.smallchangegooglemaps2;


import android.Manifest;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import android.util.Log;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.content.Intent;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.MapFragment;
import android.os.Handler;

//address
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

import com.firebase.client.Firebase;
import java.util.Map;
import java.util.HashMap;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.regex.Pattern;
import android.util.Patterns;
import android.accounts.*;
import android.content.Context;


public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap googleMap;
    private LocationManager locationManager;
    private String provider;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LatLng CurrentLatLng;
    private LatLng MapCenterLatLing;

    private TextView markerText;
    private LinearLayout markerLayout;
    private TextView UpdateTextView;
    private TextView addressText;

    public static final String TAG = MapsActivity.class.getSimpleName();

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private Firebase FirebaseRef;

    private String UsersEmailAddress = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setUpMapIfNeeded();

        Firebase.setAndroidContext(this);
        FirebaseRef = new Firebase("https://luminous-inferno-6258.firebaseio.com");


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //find the ID fields
        markerText = (TextView) findViewById(R.id.addressText);
        markerLayout = (LinearLayout) findViewById(R.id.locationMarker);
        UpdateTextView = (TextView) findViewById(R.id.MapPinText);
        addressText = (TextView) findViewById(R.id.addressText);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..


            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(0, 135, 0, 175);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        CurrentLatLng = latLng;

        //googleMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Help a homeless person here!")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.maps_marker));
        googleMap.addMarker(options);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        googleMap.animateCamera(yourLocation);

        String tempStr = ParseCurrentLocation(CurrentLatLng);
        UpdateTextView.setText(tempStr);
        addressText.setText(tempStr);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);

            googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0) {
                    // TODO Auto-generated method stub
                    MapCenterLatLing = googleMap.getCameraPosition().target;

                    markerText.setText(" Set your Location ");
                    googleMap.clear();
                    markerLayout.setVisibility(View.VISIBLE);

                    try {
                        String tempStr = ParseCurrentLocation(MapCenterLatLing);
                        UpdateTextView.setText(tempStr);
                        addressText.setText(tempStr);


                    } catch (Exception e) {
                    }
                }
            });

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    public String ParseCurrentLocation(LatLng llLocaiton) {

        String finalAddress = "";

        try {

            Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());

            List<Address> addresses = geo.getFromLocation(llLocaiton.latitude, llLocaiton.longitude, 1);
            if (addresses.isEmpty()) {
                finalAddress = "waiting for addess";
            } else {
                if (addresses.size() > 0) {

                    //TODO change this function to only return the values that are required for dynamic addresses
                    finalAddress = finalAddress + addresses.get(0).getFeatureName() + " "
                            + addresses.get(0).getThoroughfare() + ", "
                            //+ addresses.get(0).getSubLocality() + ", "
                            + addresses.get(0).getLocality() + ", "
                            //+ addresses.get(0).getPostalCode() + ", "
                            + addresses.get(0).getCountryCode();

                    /*finalAddress = finalAddress + "get address line " + addresses.get(0).getAddressLine();
                    finalAddress = finalAddress + "\nget admin area " + addresses.get(0).getAdminArea();
                    finalAddress = finalAddress + "\nget country name " + addresses.get(0).getCountryName();
                    finalAddress = finalAddress + "\nget sub admin area " + addresses.get(0).getSubAdminArea();
                    finalAddress = finalAddress + "\nget sub through " + addresses.get(0).getSubThoroughfare();*/

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalAddress;
    }


    public void SaveLocationToCloud(LatLng llLocaiton) {

        try {

            Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());

            List<Address> addresses = geo.getFromLocation(llLocaiton.latitude, llLocaiton.longitude, 1);
            if (addresses.isEmpty()) {
                //put something here
            } else {
                if (addresses.size() > 0) {
                    Firebase FirePusher = FirebaseRef.child("Homeless");

                    Map<String, String> hashAddress = new HashMap<String, String>();
                    //TODO change this function to only return the values that are required for dynamic addresses
                   /* hashAddress.put("Feature Name ", addresses.get(0).getFeatureName());
                    hashAddress.put("Thorough Fare ", addresses.get(0).getThoroughfare());
                    //+ addresses.get(0).getSubLocality());
                    hashAddress.put("Locality ", addresses.get(0).getLocality());
                    //+ addresses.get(0).getPostalCode());
                    hashAddress.put("Country Code ", addresses.get(0).getCountryCode());
                    hashAddress.put("lat ", String.valueOf(MapCenterLatLing.latitude));
                    hashAddress.put("Lng ", String.valueOf(MapCenterLatLing.longitude));*/
                    DateFormat TimeFormat = new SimpleDateFormat("HH:mm:ss");
                    DateFormat DateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
                    //get current date time with Date()
                    Date time = new Date();
                    Date date = new Date();
                    //hashAddress.put("Time ", dateFormat.format(date));

                    FirePusher.child(DateFormat.format(date) + "/Feature Name").setValue(addresses.get(0).getFeatureName());
                    FirePusher.child(DateFormat.format(date) + "/Thorough Fare").setValue(addresses.get(0).getThoroughfare());
                    FirePusher.child(DateFormat.format(date) + "/Locality ").setValue(addresses.get(0).getLocality());
                    FirePusher.child(DateFormat.format(date) + "/Country Code ").setValue(addresses.get(0).getCountryCode());
                    FirePusher.child(DateFormat.format(date) + "/lat ").setValue(String.valueOf(MapCenterLatLing.latitude));
                    FirePusher.child(DateFormat.format(date) + "/Lng ").setValue(String.valueOf(MapCenterLatLing.longitude));
                    FirePusher.child(DateFormat.format(date) + "/Time ").setValue(TimeFormat.format(time));
                    FirePusher.child(DateFormat.format(date) + "/Date ").setValue(TimeFormat.format(date));



                    /*if (UsersEmailAddress == "") {
                        UsersEmailAddress = getEmailAddress();
                        hashAddress.put("Email/Name ", UsersEmailAddress);
                    } else {
                        hashAddress.put("Email/Name ", UsersEmailAddress);
                    }*/


                    Map<String, Map<String, String>> usersName = new HashMap<String, Map<String, String>>();

                    FirePusher.push().setValue(hashAddress);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void yourLocation(View view) {

        String langstring = CurrentLatLng.toString();
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("Donation");
        alertDialog.setMessage("Would you like to make a donatin for :\n" + ParseCurrentLocation(MapCenterLatLing));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        SaveLocationToCloud(MapCenterLatLing);

           /* Firebase nFireB = FirebaseRef.child("users");
            Map<String,Object> values = new HashMap<String, String>();
            values.put("users/lat", String.valueOf(CurrentLatLng.latitude));
            values.put("users/lng", String.valueOf(CurrentLatLng.longitude));
            //Firebase setLocation = mFirebaseRef.child("location").child(String.valueOf(CurrentLatLng.latitude));
            //setLocation = mFirebaseRef.child("location").child(String.valueOf(CurrentLatLng.longitude));
            nFireB.push().setValue(values);*/
        alertDialog.show();
    }

    public String getEmailAddress() {

        String possibleEmail = "Undefined";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
            else{
                possibleEmail = "undefined";
            }
        }
        return possibleEmail;
    }
}






