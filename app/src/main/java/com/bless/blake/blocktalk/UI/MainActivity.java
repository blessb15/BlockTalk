package com.bless.blake.blocktalk.UI;

import com.bless.blake.blocktalk.Models.*;
import com.bless.blake.blocktalk.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bless.blake.blocktalk.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.newLocation)
    EditText mNewLocation;
    @Bind(R.id.MessagesView)
    ListView mMessagesView;
    @Bind(R.id.LocalMessage)
    EditText mUserMessage;
    @Bind(R.id.SubmitLocalMessage)
    Button mSubmitLocalMessage;
    @Bind(R.id.locationInfoText) TextView mLocationInfoText;
    private LocationManager locationManager;
    public static Double userLong;
    public static Double userLat;
    public static LatLng userLocation;
    private Double radius = 0.0010;
    private DatabaseReference mLocationMessagesReference;
    final ArrayList<LocationMessages> locationMessagesList = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    username = user.getDisplayName();
                    getSupportActionBar().setTitle("Hey, " + user.getDisplayName() + "!");
                } else {

                }
            }
        };

        ///REFRENCE TO DATABASE
        mLocationMessagesReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);
        final Context stuff = this;
        mLocationMessagesReference.addValueEventListener(new ValueEventListener() {

            ///DATABASE STUFF, GRAB LOCATION MESSAGES, GRAB UNIQUE KEYS TO COMPARE.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    keys.add(locationSnapshot.getKey());
                    locationMessagesList.add(locationSnapshot.getValue(LocationMessages.class));
                    for (int i = 0; i < locationMessagesList.size(); i++) {
                        if (locationMessagesList.get(i).getLatLng().latitude() <= (userLocation.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (userLocation.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (userLocation.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (userLocation.longitude() - radius)) {
                            ArrayAdapter adapter = new ArrayAdapter(stuff, android.R.layout.simple_list_item_1, locationMessagesList.get(i).getMessages());
                            mMessagesView.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2042853654178767~4761200934");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ///GRAB STUFF FROM PREVIOUS PAGE SUBMIT
        Intent intent = getIntent();
        final String newMessage = intent.getStringExtra("message");

        ///FIND USER LOCATION WITH PERMISSIONS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        ///SETS REFRESH ON USER LOCATION TO EVERY SECOND.
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                            , 10);
                }
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, listener);
        }

        ///SENDING MESSAGES
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == mSubmitLocalMessage) {
                    DatabaseReference locationMessagesRef = FirebaseDatabase
                            .getInstance()
                            .getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

                    String newLocationText = mNewLocation.getText().toString();
                    String newMessage = mUserMessage.getText().toString();

                    ///LOCAL MESSAGE SUBMIT
                    if (newMessage.length() > 0 && newLocationText.length() == 0) {
                        if(checkForNearbyLMS(locationMessagesList, userLocation) == true){
                            List<String> messages = new ArrayList<>();
                            messages.add(username + ": " + newMessage);
                            LocationMessages locationMessages = new LocationMessages(userLocation, messages);
                            mUserMessage.setText("");
                            locationMessagesRef.push().setValue(locationMessages);
                        }
                        if (locationMessagesList.size() >= 1) {
                            for (int i = 0; i < locationMessagesList.size(); i++) {
                                if (locationMessagesList.get(i).getLatLng().latitude() <= (userLocation.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (userLocation.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (userLocation.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (userLocation.longitude() - radius)) {
                                    LocationMessages newLocationMessage = locationMessagesList.get(i);
                                    newLocationMessage.getMessages().add(username + ": " + newMessage);
                                    Map<String, Object> update = new HashMap<String, Object>();
                                    update.put("messages", newLocationMessage.getMessages());
                                    locationMessagesRef.child(keys.get(i)).updateChildren(update);
                                    mUserMessage.setText("");
                                }
                            }
                        }
                    }

                    ///SEND MESSAGE TO NEW LOCATION
                    if (newLocationText.length() > 0 && newMessage.length() > 0) {
                        LatLng newLatLng = getNewUserLocation(newLocationText);
                        if(checkForNearbyLMS(locationMessagesList, newLatLng) == true){
                            List<String> messages = new ArrayList<>();
                            messages.add(username + ": " + newMessage);
                            LocationMessages locationMessages = new LocationMessages(newLatLng, messages);
                            mUserMessage.setText("");
                            locationMessagesRef.push().setValue(locationMessages);
                            Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                        }
                        for (int i = 0; i < locationMessagesList.size(); i++) {
                            if (locationMessagesList.get(i).getLatLng().latitude() <= (newLatLng.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (newLatLng.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (newLatLng.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (newLatLng.longitude() - radius)) {
                                LocationMessages newLocationMessage = locationMessagesList.get(i);
                                newLocationMessage.getMessages().add(username + ": " + newMessage);
                                Map<String, Object> update = new HashMap<String, Object>();
                                update.put("messages", newLocationMessage.getMessages());
                                locationMessagesRef.child(keys.get(i)).updateChildren(update);
                                mUserMessage.setText("");
                                Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });
    }

    ///CHECKS FOR LOCATION MESSAGES AROUND WHERE YOUR SENDING TO KEEP FROM DUPLICATION
    public boolean checkForNearbyLMS(ArrayList<LocationMessages> lms, LatLng loc) {
        boolean checkforlms = false;
        int num = 0;
        for (int i = 0; i < lms.size(); i++) {
            if (lms.get(i).getLatLng().latitude() <= (loc.latitude() + radius) && lms.get(i).getLatLng().latitude() >= (loc.latitude() - radius) && lms.get(i).getLatLng().longitude() <= (loc.longitude() + radius) && lms.get(i).getLatLng().longitude() >= (loc.longitude() - radius)) {
                num += 1;
            }
        }
        if(num == 0){
           checkforlms = true;
        }
        return checkforlms;
    }

    ///GRABS LOCATION THAT USER PROVIDES AND CONVERTS TO LATLNG
    public LatLng getNewUserLocation(String newLocation){
        Geocoder coder = new Geocoder(MainActivity.this);
        List<Address> address;

        List<LatLng> stuff = new ArrayList<LatLng>();
        LatLng newLatLng;

        try {
            address = coder.getFromLocationName(newLocation, 5);
            System.out.println("YO " + address);
            if (address == null) {
                Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
            }
            Address location = address.get(0);
            newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            stuff.add(newLatLng);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
        }
        return stuff.get(0);
    }

    public void getLocationInfo() {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

        try {
            List<Address> address = geocoder.getFromLocation(userLat, userLong, 1);
            Address userLocationInfo = address.get(0);
            mLocationInfoText.setText("Your current address: " + userLocationInfo.getAddressLine(0));
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    ///CREATES LOG OUT OPTION
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    ///LISTENER FROM DEVICES LOCATION. SETTING LAT AND LNG.
    private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {

            userLong = location.getLongitude();
            userLat = location.getLatitude();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ///REFRESHING LOCATION AND CURRENT MESSAGES VISIBLE.
                    for(int i = 0; i < locationMessagesList.size(); i++){
                        if (locationMessagesList.get(i).getLatLng().latitude() <= (userLocation.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (userLocation.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (userLocation.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (userLocation.longitude() - radius)) {
                            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, locationMessagesList.get(i).getMessages());
                            mMessagesView.setAdapter(adapter);
                        }
                    }
                    userLocation = new LatLng(userLat, userLong);
                    getLocationInfo();
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
}