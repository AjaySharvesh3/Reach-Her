package com.reachskyline.reachher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FetchDataActivity extends AppCompatActivity {

    ListView listView;
    FirebaseDatabase database;
    DatabaseReference ref, subRef;
    ArrayList<String> driveLinksList;
    ArrayAdapter<String> driveLinkArrayAdapter;
    DriveLink driveLink;
    private FirebaseAuth firebaseAuth;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_data);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        driveLink = new DriveLink();
        listView = findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
//        ref = database.getReference("DriveLinks").child(user_id).child("desc");
        ref = database.getReference();
        //subRef = ref.child("DriveLinks").child(user_id).child("desc");
        subRef = ref.getRoot().child("DriveLinks").child(user_id).child("desc");
        Log.i("hi","firebase"+subRef);
        driveLinksList = new ArrayList<>();
        driveLinkArrayAdapter = new ArrayAdapter<>(this, R.layout.fetch_drive_link_data, R.id.desc, driveLinksList);

        subRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    driveLink = ds.getValue(DriveLink.class);
                    driveLinksList.add(driveLink.getDesc() + "   " + driveLink.getLinks());
                }

                listView.setAdapter(driveLinkArrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
