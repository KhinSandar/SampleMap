package com.example.khinsandar.myapplication;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import utils.GPSTracker;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MyLocationDemoActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private LatLng currentLatLng;
    private GPSTracker gpsTracker;
    private LatLng routeTo;
    private ArrayList<Object> polylines;
    private Routing.TravelMode[] routeTypes= new Routing.TravelMode[4];

    private int currentRouteType = 0;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        routeTypes[0] = Routing.TravelMode.DRIVING;
        routeTypes[1] = Routing.TravelMode.TRANSIT;
        routeTypes[2] = Routing.TravelMode.WALKING;
        routeTypes[3] = Routing.TravelMode.BIKING;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        gpsTracker = new GPSTracker(getApplicationContext());
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            return;
        }
        //mMap.setMyLocationEnabled(true);


    }

    private void gotoCurrentLocation() {
        MarkerOptions options = new MarkerOptions();
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_nearby_maps_maker));
        options.position(currentLatLng);
        mMap.addMarker(options);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(currentLatLng)
                .zoom(15)
                .build();

        // Animate the change in camera view over 2 seconds
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),100, null);
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

            Toast.makeText(this, "Current Lat Lng" + currentLatLng, Toast.LENGTH_SHORT).show();

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            if(gpsTracker.canGetLocation() && gpsTracker.getLongitude() > 0 && gpsTracker.getLongitude() > 0) {
                currentLatLng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                Toast.makeText(this, "Current Lat Lng" + currentLatLng, Toast.LENGTH_SHORT).show();


                gotoCurrentLocation();
            }



        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        /*Intent intent = new Intent(MyLocationDemoActivity.this, MapsActivity.class);
        startActivity(intent);*/

        routeTo = new LatLng(16.849610, 96.117740);
        getRoute(routeTypes[currentRouteType], routeTo);

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

        //16.849610, 96.117740




    }

    private GoogleMap.OnMarkerClickListener onClickMarker = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            /*if(marker.getTag() != null && marker.getTag() instanceof Building){
                showBuilding((Building) marker.getTag());
            }*/
            return false;
        }
    };

    private void getRoute(Routing.TravelMode travelMode, LatLng toRoute) {
        Routing routing = new Routing.Builder()
                .travelMode(travelMode)
                .withListener(new RoutingListener() {
                    @Override
                    public void onRoutingFailure(RouteException e) {
                        //showLoading(false);
                        Toast.makeText(getApplicationContext(), "This routing is not available.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRoutingStart() {

                    }

                    @Override
                    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
                        //showLoading(false);
                        mMap.clear();
                        gotoCurrentLocation();

                        mMap.addMarker(new MarkerOptions()
                                .position(routeTo) //new LatLng(building.getLat(), building.getLng())
                                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_nearby_maps_maker)));

                                //.icon(bitmapDescriptorFromVector(getActivity(), getMaker(building.getType())))).setTag(building);
                        /*for(Building building: buildings) {

                        }
                        if(slidingPanel.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            recyclerView.smoothScrollToPosition(0);
                            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }*/
                        //mMap.setOnMarkerClickListener(onClickMarker);

                        polylines = new ArrayList<>();
                        //add route(s) to the map.
                        double distance = 0.0;
                        for (int i = 0; i < routes.size(); i++) {
                            PolylineOptions polyOptions = new PolylineOptions();
                            polyOptions.color(getResources().getColor(R.color.colorAccent));
                            polyOptions.width(16);
                            polyOptions.addAll(routes.get(i).getPoints());
                            Polyline polyline = mMap.addPolyline(polyOptions);
                            polylines.add(polyline);
                            distance += routes.get(i).getDistanceValue();
                        }
                    }

                    @Override
                    public void onRoutingCancelled() {

                    }
                })
                .waypoints(currentLatLng, toRoute)
                .build();
        routing.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
