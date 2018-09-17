package com.example.manish.fuelpumplocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.manish.fuelpumplocator.Model.MyPlaces;
import com.example.manish.fuelpumplocator.Model.PlaceDetail;
import com.example.manish.fuelpumplocator.Model.Results;
import com.example.manish.fuelpumplocator.Remote.IGoogleAPIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        {

    private static final int MY_PERMISSION_CODE =1000;
    private String newurl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private GoogleMap mMap;

    private double latitude,longitude;
    private Location mLastLocation;
    private Marker mMarker;

    IGoogleAPIService mServices;

    MyPlaces currentPlace;

    //New location

            FusedLocationProviderClient fusedLocationProviderClient;
            LocationCallback locationCallback;
            private LocationRequest mLocationRequest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //init Service
        mServices=Common.getGoogleAPIService();
         // request runtime permission
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        BottomNavigationView bottomNavigationView=( BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.action_hospital:
                        nearByPlaces("hospital");
                        break;
                    case R.id.action_market:
                        nearByPlaces("market");
                        break;
                    case R.id.action_restaurant:
                        nearByPlaces("restaurant");
                        break;
                    case R.id.action_school:
                        nearByPlaces("school");
                        break;
                        default:
                            break;
                }
                return true;
            }
        });

        //Init location

        buildLocationCallBack();
        buildLocationRequest();

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());

    }

            @Override
            protected void onStop() {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                super.onStop();
            }

            private void buildLocationRequest() {

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(1000);
                mLocationRequest.setSmallestDisplacement(10f);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }

            private void buildLocationCallBack() {
        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation=locationResult.getLastLocation();

                if (mMarker != null)
                    mMarker.remove();

                latitude= mLastLocation.getLatitude();
                longitude= mLastLocation.getLongitude();

                LatLng latLng =new LatLng(latitude,longitude);
                MarkerOptions markerOptions= new MarkerOptions()
                        .position(latLng)
                        .title("your position")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMarker = mMap.addMarker(markerOptions);
                //move camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        };

            }

            private void nearByPlaces(final String placeType) {
        mMap.clear();
        String url= getUrl(latitude,longitude,placeType);

        mServices.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {

                        currentPlace=response.body();   //remember assign values for current place

                        if (response.isSuccessful() && currentPlace != null)
                        {
                            for(int i=0;i<response.body().getResults().length;i++)
                            {
                                MarkerOptions markerOptions= new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat= Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng= Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placeName=googlePlace.getName();
                                String vicinity= googlePlace.getVicinity();
                                LatLng latLng= new LatLng(lat,lng);
                                markerOptions.position(latLng);
                                markerOptions.title(placeName);
                                if (placeType.equals("hospital"))
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
                                else
                                if (placeType.equals("market"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_market));
                                else
                                if (placeType.equals("restaurant"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant));
                                else
                                if (placeType.equals("school"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_school));
                                else
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                markerOptions.snippet(String.valueOf(i));   //assign index for market
                                //add to map
                                mMarker= mMap.addMarker(markerOptions);

                                // move camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });

    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlacesUrl=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+10000);
        googlePlacesUrl.append("&type="+placeType);
        //googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key="+getResources().getString(R.string.browser_key));
        Log.d("getUrl",googlePlacesUrl.toString());
        return googlePlacesUrl.toString();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION ))
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },MY_PERMISSION_CODE);
            else
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },MY_PERMISSION_CODE);
            return false;
        }
        else
            return true;

    }

    //ctrl+o


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_CODE:
            {
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                    {

                        mMap.setMyLocationEnabled(true);

                        //Init location

                        buildLocationCallBack();
                        buildLocationRequest();

                        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);
                        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());
                    }
                }

            }
            break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //init google play services
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
            } else {

                mMap.setMyLocationEnabled(true);

            }

        }

            //make event click on marker
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.getSnippet() != null ) {

                            //when user select marker , just get result of place and assign to static variable
                            Common.currentResult = currentPlace.getResults()[Integer.parseInt(marker.getSnippet())];
                            //start new activity
                            startActivity(new Intent(MapsActivity.this, ViewPlace.class));
                        }
                        return true;
                    }
                });
        }


}
