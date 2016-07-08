package com.example.blake.blocktalk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class UserActivity extends FragmentActivity implements OnMapReadyCallback {
    @Bind(R.id.GetUser)
    TextView mGetUser;
    @Bind(R.id.MessagesView)
    ListView mMessagesView;
    private String[] messages = {"Bill: hey", "Jim: sup", "Michaela: hi",
            "Blake: yo", "Jim: sup", "Blake: yo"};
    private GoogleMap mMap;
    LocationManager locationManager;
    Double userLong;
    Double userLat;
    LatLng userLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        mGetUser.setText("Hey, " + username + "!");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        mMessagesView.setAdapter(adapter);
        ////////location stuff//////////
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                            , 10);
                }
                return;
            }
            Toast.makeText(UserActivity.this, "IN IF STATEMENT", Toast.LENGTH_SHORT).show();

            locationManager.requestLocationUpdates(provider, 1000, 0, listener);

        }
    }

   private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {
            userLong = location.getLongitude();
            userLat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UserActivity.this, "IN RUN METHOD", Toast.LENGTH_SHORT).show();
                    userLocation = new LatLng(userLat, userLong);
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
            userLocation = new LatLng(userLat, userLong);
        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Toast.makeText(UserActivity.this, "IN ONMAPREADY METHOD", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
    }
}