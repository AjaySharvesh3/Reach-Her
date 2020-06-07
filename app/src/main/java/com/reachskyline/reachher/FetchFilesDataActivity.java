package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class FetchFilesDataActivity extends AppCompatActivity {

    ListView listView;
    FirebaseDatabase database;
    TextView noDataEditText;
    DatabaseReference ref, subRef;
    ArrayList<String> driveLinksList;
    ArrayAdapter driveLinkArrayAdapter;
    DriveLink driveLink;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String user_id, user_name, phone_number, name, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_files_data);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();
        driveLink = new DriveLink();
        listView = findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();

        noDataEditText = findViewById(R.id.noDataText);

        driveLinksList = new ArrayList<>();
        driveLinkArrayAdapter = new ArrayAdapter<>(this, R.layout.fetch_file_details, R.id.desc, driveLinksList);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        name = task.getResult().getString("name");
                        phone = task.getResult().getString("phone");

                        ref = database.getReference();
                        subRef = ref.child("UploadedFiles").child(name + "_" + phone);

                        subRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("hiiiii_datasnapshot:::", ""+dataSnapshot.getValue());
                                if (dataSnapshot.getValue() == null) {
                                    noDataEditText.setVisibility(View.VISIBLE);
                                    return;
                                }
                                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                                    JSONObject json = new JSONObject((Map) ds.getValue());
                                        try {
                                            driveLinksList.add("Title: " + json.getString("title") + "\n" + "Description: " + json.getString("desc"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                }
                                listView.setAdapter(driveLinkArrayAdapter);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(FetchFilesDataActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
