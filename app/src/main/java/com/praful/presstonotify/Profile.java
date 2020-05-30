package com.praful.presstonotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    TextView parentno, emergno, policeno;
    Button update;
    String uid;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        parentno = findViewById(R.id.parentno);
        emergno = findViewById(R.id.emergencyno);
        policeno = findViewById(R.id.policeno);
        update = findViewById(R.id.update);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users info")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> map1 = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (map1 != null) {
                            if(map1.get("parentno") != null){
                                parentno.setText(String.valueOf(map1.get("parentno")));

                            }
                            if(map1.get("emergno") != null){
                                emergno.setText(String.valueOf(map1.get("emergno")));

                            }
                            if(map1.get("policeno") != null){
                                policeno.setText(String.valueOf(map1.get("policeno")));

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("parentno", parentno.getText().toString());
                map.put("policeno", policeno.getText().toString());
                map.put("emergno", emergno.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("Users info")
                        .child(uid)
                        .updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
