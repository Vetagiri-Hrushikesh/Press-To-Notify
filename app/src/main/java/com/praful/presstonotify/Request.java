package com.praful.presstonotify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Request extends AppCompatActivity {

    TextView name, type, message;
    Button cancel, confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        name = (TextView)findViewById(R.id.name);
        type = (TextView)findViewById(R.id.type);
        message = (TextView)findViewById(R.id.msg);
        cancel = (Button) findViewById(R.id.cancel);
        confirm = (Button) findViewById(R.id.confirm);
        final Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        type.setText(intent.getStringExtra("type"));
        message.setText(intent.getStringExtra("msg"));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String, Object> map0 = new HashMap<>();
                map0.put("uid",uid);
                map0.put("msg", FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + " is comming to save! from " + intent.getStringExtra("distance")+" km");
                map0.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                FirebaseDatabase.getInstance().getReference()
                        .child("Response")
                        .child(intent.getStringExtra("uid"))
                        .child(uid)
                        .setValue(map0).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        double lat = Double.parseDouble(intent.getStringExtra("lat"));
                        double lng = Double.parseDouble(intent.getStringExtra("lng"));
                        Uri navigationIntentUri = Uri.parse("google.navigation:q=" + lat + " , " + lng + "&mode=d");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);

                        FirebaseDatabase.getInstance().getReference()
                                .child("Response")
                                .child(intent.getStringExtra("uid"))
                                .child(uid)
                                .removeValue();
                    }
                });
                finish();

            }
        });
    }
}
