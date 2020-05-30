package com.praful.presstonotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialOverlayLayout;
import com.leinardi.android.speeddial.SpeedDialView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.praful.presstonotify.MainActivity.My_PERMISSION;

public class Home extends AppCompatActivity implements DuoMenuView.OnMenuClickListener, OnMapReadyCallback {

    Toolbar mToolbar;
    private GoogleMap mMap;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    double distance = 0.01;
    private static final int LIMIT = 10;
    String uid;

    DuoDrawerLayout mDuoDrawerLayout;
    DuoMenuView mDuoMenuView;


    LocationManager lm;
    SupportMapFragment mapFragment;
    int x = 0;
    TextView username;
    ArrayList<String> st = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Paper.init(this);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mDuoDrawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDuoMenuView = (DuoMenuView) mDuoDrawerLayout.getMenuView();
        mDuoMenuView.setOnMenuClickListener(this);
        DuoDrawerToggle duoDrawerToggle = new DuoDrawerToggle(this,
                mDuoDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        LinearLayout signout = (LinearLayout) mDuoMenuView.getHeaderView().findViewById(R.id.signout);
        LinearLayout profile = (LinearLayout) mDuoMenuView.getHeaderView().findViewById(R.id.profile);
        username = (TextView) mDuoMenuView.getHeaderView().findViewById(R.id.username);
        CircleImageView userimg = (CircleImageView) mDuoMenuView.getHeaderView().findViewById(R.id.userimg);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS


            }, My_PERMISSION);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String gmail = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            Picasso.with(this).load(photoUrl).into(userimg);
            username.setText(name);

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDuoDrawerLayout.setDrawerListener(duoDrawerToggle);
        duoDrawerToggle.syncState();

        duoDrawerToggle.setDrawerIndicatorEnabled(true);


        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDuoDrawerLayout.closeDrawer();
                new CountDownTimer(200, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {


                        signOut();
                        finish();
                    }
                }.start();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDuoDrawerLayout.closeDrawer();
                new CountDownTimer(200, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {


                        Intent intent = new Intent(getBaseContext(), Profile.class);
                        startActivity(intent);
                    }
                }.start();
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Button high = (Button) findViewById(R.id.high);

        high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String scAddress = null;
                final PendingIntent sentIntent = null, deliveryIntent = null;
                final SmsManager smsManager = SmsManager.getDefault();

                FirebaseDatabase.getInstance().getReference().child("Users info")
                        .child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Object> map1 = (HashMap<String, Object>) dataSnapshot.getValue();
                                if (map1 != null) {

                                    smsManager.sendTextMessage(String.valueOf(map1.get("parentno")), scAddress, username.getText().toString() + " is in emergency !"
                                            + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),
                                            sentIntent, deliveryIntent);
                                    smsManager.sendTextMessage(String.valueOf(map1.get("policeno")), scAddress, username.getText().toString() + " is in emergency !"
                                                    + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),
                                            sentIntent, deliveryIntent);
                                    smsManager.sendTextMessage(String.valueOf(map1.get("emergno")), scAddress, username.getText().toString() + " is in emergency !"
                                                    + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),

                                            sentIntent, deliveryIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                distance = 0.01;
                x = 0;
                sendnotify("high");
                st.clear();
                Toast.makeText(Home.this, "Request sent upto 10km to 10 users and to your profile numbers, hang on!", Toast.LENGTH_LONG).show();


            }
        });

        Button low = (Button) findViewById(R.id.low);

        low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String scAddress = null;
                final PendingIntent sentIntent = null, deliveryIntent = null;

                final SmsManager smsManager = SmsManager.getDefault();

                FirebaseDatabase.getInstance().getReference().child("Users info")
                        .child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Object> map1 = (HashMap<String, Object>) dataSnapshot.getValue();
                                if (map1 != null) {

                                    smsManager.sendTextMessage(String.valueOf(map1.get("parentno")), scAddress, username.getText().toString() + " is in emergency !"
                                                    + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),
                                            sentIntent, deliveryIntent);
                                    smsManager.sendTextMessage(String.valueOf(map1.get("policeno")), scAddress, username.getText().toString() + " is in emergency !"
                                                    + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),
                                            sentIntent, deliveryIntent);
                                    smsManager.sendTextMessage(String.valueOf(map1.get("emergno")), scAddress, username.getText().toString() + " is in emergency !"
                                                    + "https://www.google.com/maps/dir/?api=1&travelmode=driving&layer=traffic&destination="+String.valueOf(map1.get("lat"))+","+String.valueOf(map1.get("lng")),

                                            sentIntent, deliveryIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                distance = 0.01;
                x = 0;
                st.clear();
                sendnotify("low");
                Toast.makeText(Home.this, "Request sent upto 10km to 10 users and to your profile numbers, hang on!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendnotify(final String type) {


        GeoFire gf = new GeoFire(FirebaseDatabase.getInstance().getReference("Users locations"));
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Double lat = Double.parseDouble(pref.getString("lat", ""));
        Double lng = Double.parseDouble(pref.getString("lng", ""));
        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(lat, lng), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                if (key != FirebaseAuth.getInstance().getCurrentUser().getUid() && st.contains(key) == false) {
                    st.add(key);
                    x++;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd | HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    Map<String, Object> map0 = new HashMap<>();
                    map0.put("uid", mAuth.getUid());
                    map0.put("msg", username.getText().toString() + " is in emergency ! within " + distance);
                    map0.put("date", currentDateandTime);
                    map0.put("name", username.getText().toString());
                    map0.put("lat", pref.getString("lat", ""));
                    map0.put("lng", pref.getString("lng", ""));
                    map0.put("type", type);
                    map0.put("distance", distance);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Request")
                            .child(key)
                            .child(uid)
                            .setValue(map0);
                    FirebaseDatabase.getInstance().getReference()
                            .child("Request")
                            .child(key)
                            .child(uid)
                            .removeValue();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (x < 11 && distance < LIMIT) {

                    distance += 0.01;
                    sendnotify(type);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }


    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    @Override
    public void onFooterClicked() {

    }

    @Override
    public void onHeaderClicked() {

    }

    @Override
    public void onOptionClicked(int position, Object objectClicked) {

    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // mMap.setPadding(left, top, right, bottom);
        mMap.setPadding(0, 50, 41, 0);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        // Add a marker in Sydney and move the camera
        LatLng india = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(india).title("Marker in Sydney"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .target(india)
                .zoom(15.0f)// Sets the center of the map to Mountain View
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));


    }

}
