package com.example.sharvani.mapsapp;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements StoreLocationsFragment.Communicator {

    static GoogleMap map; // Might be null if Google Play services APK is not available.
    static String name;
    String firstChar;
    TextView storeName;
    //static GoogleMap map;
    LocationManager locationManager;
    List<Address> addresses;
    Geocoder gcd;
    static String zipCode="94582";
    static String[] Addresses = new String[2];
    static double lat, lat1;
    static double lon, lon1;

    StoreLocationsFragment storeLocFrag;
    FragmentManager manager;
    MarkerOptions markerOptions;
    private LatLng fromPosition;
    private LatLng toPosition;
    Document document;
    EditText addressBox;
    GMapV2GetRouteDirection v2GetRouteDirection = new GMapV2GetRouteDirection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        name = intent.getStringExtra("Name");
        firstChar = name.substring(0,1).toUpperCase();
        name = firstChar + name.substring(1);
        storeName = (TextView) findViewById(R.id.storeNameInFindLoc);
        storeName.setText("Nearest " + name + " Locations");
        setTitle( name + " Locations");
        markCurLoc();


        addressBox = (EditText) findViewById(R.id.editText1);
        manager = getFragmentManager();
        storeLocFrag = (StoreLocationsFragment) manager
                .findFragmentById(R.id.storeLocationsFragment);
        storeLocFrag.setCommunicator(this);

        // MAP Marking Current Location
    }

    public void markCurLoc() {

        gcd = new Geocoder(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

        map = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();

        LatLng MyLoc = new LatLng(location.getLatitude(),
                location.getLongitude());

        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 10);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Address address = addresses.get(0);

        zipCode = address.getPostalCode();
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLoc, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(MyLoc) // Sets the center of the map to
                        // location user
                .zoom(17) // Sets the zoom
                .bearing(90) // Sets the orientation of the camera to east
                .tilt(40) // Sets the tilt of the camera to 30 degrees
                .build(); // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        map.addMarker(new MarkerOptions().title("A").snippet("Your Location")
                .position(MyLoc));
        Toast.makeText(this, "Zip : " + zipCode, Toast.LENGTH_SHORT).show();

    }

    public void markLoc(String address) {
        this.convertAddress(address);

        LatLng MyLoc = new LatLng(lat, lon);

        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLoc, 10));

        map.addMarker(new MarkerOptions().title("A").snippet("Your Location")
                .position(MyLoc));

    }

    public void convertAddress(String address) {

        Geocoder geoCoder = new Geocoder(this);
        if (address != null && !address.isEmpty()) {
            try {
                List<Address> addressList = geoCoder.getFromLocationName(
                        address, 1);
                if (addressList != null && addressList.size() > 0) {
                    lat = addressList.get(0).getLatitude();
                    lon = addressList.get(0).getLongitude();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } // end catch
        } // end if
    }




    public static void setMapPoints(String[] arrayStr) {


    }

    @Override
    public void respond(String[] strArr) {
        for (String s : strArr)
            markLoc(s);
    }

    @Override
    public void respondName(String str) {
        String address = str;
        addressBox.setText(address);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        fromPosition = new LatLng(location.getLatitude(),
                location.getLongitude());
        // Get a handle to the Map Fragment
        this.convertAddress1(address);

        LatLng geopoint2 = new LatLng(lat1, lon1);

        map.setMyLocationEnabled(true);

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.setTrafficEnabled(true);
        map.animateCamera(CameraUpdateFactory.zoomTo(12));
        markerOptions = new MarkerOptions();

        toPosition = geopoint2;

        GetRouteTask getRoute = new GetRouteTask();
        getRoute.execute();
    }

    public void convertAddress1(String address) {

        Geocoder geoCoder = new Geocoder(this);
        if (address != null && !address.isEmpty()) {
            try {
                List<Address> addressList = geoCoder.getFromLocationName(
                        address, 1);
                if (addressList != null && addressList.size() > 0) {
                    lat1 = addressList.get(0).getLatitude();
                    lon1 = addressList.get(0).getLongitude();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } // end catch
        } // end if
    }

    private class GetRouteTask extends AsyncTask<String, Void, String> {

        private ProgressDialog Dialog;
        String response = "";

        @Override
        protected void onPreExecute() {

            Dialog = new ProgressDialog(MapsActivity.this);
            Dialog.setMessage("Loading route...");
            Dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            // Get All Route values

            document = v2GetRouteDirection.getDocument(fromPosition,
                    toPosition, GMapV2GetRouteDirection.MODE_DRIVING);

            response = "Success";
            return response;

        }

        @Override
        protected void onPostExecute(String result) {

            map.clear();
            if (response.equalsIgnoreCase("Success")) {
                ArrayList<LatLng> directionPoint = v2GetRouteDirection
                        .getDirection(document);
                PolylineOptions rectLine = new PolylineOptions().width(10)
                        .color(Color.BLUE);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                // Adding route on the map
                map.addPolyline(rectLine);
                markerOptions.position(toPosition);
                markerOptions.draggable(true);
                map.addMarker(markerOptions);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(toPosition) // Sets the center of the map to
                                // location user
                        .zoom(12) // Sets the zoom
                        .bearing(90) // Sets the orientation of the camera to
                                // east
                        .tilt(40) // Sets the tilt of the camera to 30 degrees
                        .build(); // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

                map.addMarker(new MarkerOptions().title("B")
                        .snippet("Your Store").position(toPosition));
                map.addMarker(new MarkerOptions().title("A")
                        .snippet("Your Current Location")
                        .position(fromPosition));
            }

            Dialog.dismiss();
        }
    }

    public void phoneMapFn(View v) {
        String address = addressBox.getText().toString();
        this.convertAddress(address);
        String uri = String.format(Locale.ENGLISH,
                "http://maps.google.com/maps?daddr=%f,%f (%s)", lat, lon, name
                        + " Location " + address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(uri));
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(this, "Please install a maps application",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
