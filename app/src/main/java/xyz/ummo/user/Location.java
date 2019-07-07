package xyz.ummo.user;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import xyz.ummo.user.adapters.PlaceAutocompleteAdapter;
import xyz.ummo.user.delegate.UpdateUserLocation;
import xyz.ummo.user.ui.MainScreen;
import xyz.ummo.user.utilities.PrefManager;

import static android.text.Html.fromHtml;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class Location extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {




    private GoogleMap mMap;
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    android.location.Location mLastLocation;
    Marker mCurrLocationMarker;
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;


    private AutoCompleteTextView searchLocationTxt;
    private PlaceAutocompleteAdapter mAdapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setTitle("Select your location");


        if(!Places.isInitialized()){

            Places.initialize(this, "AIzaSyDBd47IkfyqjEO4lgrb59cs-4ycRulrztc");

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

//        assign search location textview
        searchLocationTxt = findViewById(R.id.search_location);

        searchLocationTxt.setOnItemClickListener(mAutocompleteClickListener);

//        instantiate the PlaceAutocompleteAdapter passing the context as an argument
        mAdapter = new PlaceAutocompleteAdapter(this);

        //      set the search location textview an adapter for auto suggesting places
        searchLocationTxt.setAdapter(mAdapter);
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

        checkGPSNetworkEnabled();

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

                init();

            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                //Request Location Permission
                checkLocationPermission();
                init();
            }

            init();
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    public void confirmLocation(View view){

        Intent i= new Intent(this, MainScreen.class);
        finish();
        startActivity(i);
    }

    @Override
    public void onMyLocationClick(@NonNull android.location.Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(android.location.Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        storeLatLong(lat, lng);
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Location.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void init(){

        mAdapter = new PlaceAutocompleteAdapter(this);
        searchLocationTxt.setAdapter(mAdapter);


        searchLocationTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){


                    searchLocation();
                }

                return false;
            }
        });
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = (PlaceAutocompleteAdapter.PlaceAutocomplete) mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            searchLocation();

            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
//            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
//            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);


            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                // Format details of the place for display and show it in a TextView.

                Toast.makeText(getApplicationContext(), formatPlaceDetails(getResources(), place.getName(),
                        place.getId(), place.getAddress(), place.getPhoneNumber(),
                        place.getWebsiteUri()),
                        Toast.LENGTH_SHORT).show();

                // Display the third party attributions if set.
                final CharSequence thirdPartyAttribution = places.getAttributions();
                if (thirdPartyAttribution == null) {
                } else {

                    Toast.makeText(getApplicationContext(), fromHtml(thirdPartyAttribution.toString()),
                            Toast.LENGTH_SHORT).show();
                }

                Log.i(TAG, "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    public void searchLocation(){


//      get the search string and assign it to the string address
        String address = searchLocationTxt.getText().toString();

        List<Address> addressList = null;

//      instantiate the MarkerOptions class
        MarkerOptions userMarkerOptions = new MarkerOptions();


//        check if the address is no null
        if(!TextUtils.isEmpty(address)){

//            Instantiate the geocoder class ( to change the address to real places on the world map)
            Geocoder geocoder = new Geocoder(this);

            try {

//                assign the addressList to a maximum of 6 results from the address passed by the user
                addressList = geocoder.getFromLocationName(address, 6);

//                check if address is not null again
                if ( address != null){

                    for (int i = 0; i <addressList.size(); i++){

//                        assign Address to the one from the addressList
                        Address userAddress = addressList.get(i);

//                        instantiate the Latitude and longitude based of the address
                        LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());

//                        position the maker
                        userMarkerOptions.position(latLng);

//                        set the title of the marker
                        userMarkerOptions.title(address);

//                        set the marker icon
                        userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

//                        add the marker on the map
                        mMap.addMarker(userMarkerOptions);

                        //move map camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

//                        animate the camera movement
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    }
                }else{

//                    if address is null issue a message that location is not found
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{

//            if the search location textview is empty prompt the user to input any location

            Toast.makeText(this, " please write any location name", Toast.LENGTH_SHORT).show();

        }
    }

    public void checkGPSNetworkEnabled(){

//        LocationManager class provides access to system location services
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {

//            returnS the current enabled/disabled status of the GPS _PROVIDER
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch(Exception ex) {}

        try {

//            return the current enabled/disabled status of the NETWORK_PROVIDER
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch(Exception ex) {}

//        Checks if location in the system is enabled or disabled
        if(!gps_enabled && !network_enabled) {

            // notify user
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("GPS Network not enabled")
                    .setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {

//                            opens location settings
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", null).create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                    btnPositive.setTextSize(13);

                    Button btnNegative = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
                    btnNegative.setTextSize(13);
                }
            });

            alertDialog.show();

        }
    }

    public void storeLatLong(Double lat, Double lng){

        SharedPreferences latLngPref = getSharedPreferences("LatLngPref", MODE_PRIVATE);
        SharedPreferences.Editor prefEditor;
        prefEditor = latLngPref.edit();
        prefEditor.putFloat("lat", lat.floatValue());
        prefEditor.putFloat("lng", lng.floatValue());
        prefEditor.apply();


            String _id = new PrefManager(this).getUserId();
            new UpdateUserLocation(this,lat,lng,_id){
                @Override
                public void done(@NotNull byte[] data, @NotNull Number code) {
                    Log.e("Location",""+code);
                }
            };



//
//
//        float latPref  = latLngPref.getFloat("lat", 0);
//        float lngPref = latLngPref.getFloat("lng", 0);
//
//        Toast.makeText(this, latPref + " , " +lngPref + "", Toast.LENGTH_LONG).show();

    }
}


