package com.praful.presstonotify;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class MyService extends Service {

    public MyService() {
    }

    GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("Users locations"));
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LatLng latLng;
    GeoQuery geoQuery;
    double lat, lng;
    Location location;
    final String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    SharedPreferences pref;

    @Override
    public void onCreate() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final NotificationHelper1 notificationHelper = new NotificationHelper1(getBaseContext());


            FirebaseDatabase.getInstance().getReference()
                    .child("Request")
                    .child(id)
                    .addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot1.getValue();
                                if (map != null && map.get("uid")!= null&&  id != map.get("uid")) {

                                    String lat = String.valueOf(map.get("lat"));
                                    String lng = String.valueOf(map.get("lng"));
                                    String type = String.valueOf(map.get("type"));
                                    String name = String.valueOf(map.get("name"));
                                    String msg = String.valueOf(map.get("msg"));
                                    String date = String.valueOf(map.get("date"));
                                    String uid = String.valueOf(map.get("uid"));
                                    String distance = String.valueOf(map.get("distance"));

                                    Intent responseI = new Intent(getBaseContext(), Request.class);
                                    responseI.putExtra("type", String.valueOf(map.get("type")));
                                    responseI.putExtra("uid", uid);
                                    responseI.putExtra("name", name);
                                    responseI.putExtra("lat", lat);
                                    responseI.putExtra("lng", lng);
                                    responseI.putExtra("date", date);
                                    responseI.putExtra("msg", name + " needs your help, within " + distance+" km");
                                    responseI.putExtra("distance", distance);
                                    final PendingIntent responseIp = PendingIntent.getActivity(getBaseContext(), 0,
                                            responseI, PendingIntent.FLAG_UPDATE_CURRENT);
                                    Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                    Notification.Builder builder = notificationHelper.getUberNotification("Press To Notify Request got! " + type,
                                            name + " needs your help, within " + distance+" km", responseIp, sound);

                                    Notification notification = builder.build();
                                    notificationHelper.getManager().notify(1, notification);

                                    Intent intent = new Intent(getBaseContext(), Request.class);
                                    intent.putExtra("name", name);
                                    intent.putExtra("type", type);
                                    intent.putExtra("lat", lat);
                                    intent.putExtra("lng", lng);
                                    intent.putExtra("date", date);
                                    intent.putExtra("msg", name + " needs your help, within " + distance+" km");
                                    intent.putExtra("distance", distance);
                                    intent.putExtra("uid", uid);

                                    startActivity(intent);

                                    FirebaseDatabase.getInstance().getReference().child("Request")
                                            .child(id).child(dataSnapshot1.getKey()).removeValue();
                                }

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            FirebaseDatabase.getInstance().getReference()
                    .child("Response")
                    .child(id)
                    .addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot1.getValue();
                                if (map != null && id != dataSnapshot1.getKey()) {

                                    String msg = String.valueOf(map.get("msg"));

                                    Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                    Notification.Builder builder = notificationHelper.getUberNotification("Press To Notify",
                                            msg, null, sound);

                                    Notification notification = builder.build();
                                    notificationHelper.getManager().notify(1, notification);

                                    FirebaseDatabase.getInstance().getReference().child("Response")
                                            .child(id).child(dataSnapshot1.getKey()).removeValue();
                                }

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            Intent notificationIntent = new Intent(this, Home.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            ;
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            Notification.Builder builder = notificationHelper.getUberNotification("Press To Notify", "This is for notify you without any delay", null, sound);
            final Notification notification = builder.build();
            startForeground(1337, notification);

        } else {


            FirebaseDatabase.getInstance().getReference()
                    .child("Request")
                    .child(id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot1.getValue();
                                if (map != null && map.get("uid")!= null&&  id != map.get("uid")) {


                                    String lat = String.valueOf(map.get("lat"));
                                    String lng = String.valueOf(map.get("lng"));
                                    String type = String.valueOf(map.get("type"));
                                    String name = String.valueOf(map.get("name"));
                                    String date = String.valueOf(map.get("date"));
                                    String msg = String.valueOf(map.get("msg"));
                                    String distance = String.valueOf(map.get("distance"));
                                    String uid = String.valueOf(map.get("uid"));

                                    Intent responseI = new Intent(getBaseContext(), Request.class);
                                    responseI.putExtra("type", String.valueOf(map.get("type")));
                                    responseI.putExtra("lat", lat);
                                    responseI.putExtra("lng", lng);
                                    responseI.putExtra("date", date);
                                    responseI.putExtra("msg", name + " needs your help, within " + distance+" km");
                                    responseI.putExtra("name", name);
                                    responseI.putExtra("distance", distance);
                                    responseI.putExtra("uid", uid);

                                    final PendingIntent responseIp = PendingIntent.getActivity(getBaseContext(), 0,
                                            responseI, PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
                                    builder.setAutoCancel(true)
                                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                            .setWhen(System.currentTimeMillis())
                                            .setSmallIcon(R.drawable.personpin)
                                            .setContentTitle("Press To Notify Request got! " + type)
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(name + " needs your help, within " + distance+" km"));


                                    NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    manager.notify(1, builder.build());

                                    Intent intent = new Intent(getBaseContext(), Request.class);
                                    intent.putExtra("name", name);
                                    intent.putExtra("type", type);
                                    intent.putExtra("lat", lat);
                                    intent.putExtra("lng", lng);
                                    intent.putExtra("date", date);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("msg", name + " needs your help, within " + distance+" km");
                                    intent.putExtra("distance", distance);
                                    startActivity(intent);

                                    FirebaseDatabase.getInstance().getReference().child("Request")
                                            .child(id).child(dataSnapshot1.getKey()).removeValue();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            FirebaseDatabase.getInstance().getReference()
                    .child("Response")
                    .child(id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot1.getValue();
                                if (map != null && id != dataSnapshot1.getKey()) {


                                    String msg = String.valueOf(map.get("msg"));

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
                                    builder.setAutoCancel(true)
                                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                            .setWhen(System.currentTimeMillis()).
                                            setSmallIcon(R.drawable.personpin)
                                            .setContentTitle("Press To Notify ! ")
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));

                                    NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    manager.notify(1, builder.build());

                                    FirebaseDatabase.getInstance().getReference().child("Response")
                                            .child(id).child(dataSnapshot1.getKey()).removeValue();

                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            Intent notificationIntent = new Intent(this, Home.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setWhen(System.currentTimeMillis()).
                    setSmallIcon(R.drawable.personpin)
                    .setContentTitle("Press To Notify")
                    .setContentText("This is for notify you without any delay");
//                    .setContentIntent(pendingIntent);

            final Notification notification = builder.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                startForeground(1337, notification);
            }


        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        buildLocationreq();
        buildLocationCallback();
        displaylocation();
        FirebaseDatabase.getInstance().getReference()
                .child("Users info")
                .child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (map != null) {
                            String lat = String.valueOf(map.get("lat"));
                            String lng = String.valueOf(map.get("lng"));
                            if (map.get("lat") != null && map.get("lng")!=null && lat.length() != 0 && lng.length() != 0) {
                                Double lat1 = Double.parseDouble(lat);
                                Double lng1 = Double.parseDouble(lng);
                                geoQuery = geoFire.queryAtLocation(new GeoLocation(lat1, lng1), 0.02);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, (com.google.android.gms.location.LocationCallback) locationCallback, Looper.myLooper());


    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for (Location location1 : locationResult.getLocations()) {

                    location = location1;
                }


                displaylocation();
            }
        };
    }


    private void buildLocationreq() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void displaylocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location1) {
                location = location1;
                if (location != null) {

                    geoFire.setLocation(id, new GeoLocation(location.getLatitude(), location.getLongitude())
                            , new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("lat", String.valueOf(location.getLatitude()));
                                    editor.putString("lng", String.valueOf(location.getLongitude()));
                                    editor.apply();
                                    final Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("lat", String.valueOf(location.getLatitude()));
                                    map.put("lng", String.valueOf(location.getLongitude()));

                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users info")
                                            .child(id)
                                            .updateChildren(map);


                                }


                            });
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseDatabase.getInstance().goOnline();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    @RequiresApi(api = Build.VERSION_CODES.O)


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

