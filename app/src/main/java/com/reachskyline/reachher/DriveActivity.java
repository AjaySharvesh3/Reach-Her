package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DriveActivity extends AppCompatActivity {

    private String user_id, name, phone;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference rootReference, demoReference, reference;

    private Button driveLinkUploadBtn;
    private EditText driveLinkEditText;
    private EditText descriptionEditText;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        rootReference = FirebaseDatabase.getInstance().getReference();
        demoReference = rootReference.child("UploadedDriveLinks");

        driveLinkUploadBtn = findViewById(R.id.driveLinkUpload);
        driveLinkEditText = findViewById(R.id.driveLinkEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        name = task.getResult().getString("name");
                        phone = task.getResult().getString("phone");
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(DriveActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        driveLinkUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("Uploading your file...");
                progressDialog.show();
                final String driveLink = driveLinkEditText.getText().toString();
                final String description = descriptionEditText.getText().toString();
                if (!TextUtils.isEmpty(driveLink) && !TextUtils.isEmpty(description)) {
                    user_id = firebaseAuth.getCurrentUser().getUid();
                    reference = demoReference.child(name + "_" + phone).push();
                    reference.child("desc").setValue(description);
                    reference.child("link").setValue(driveLink).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DriveActivity.this, "Video Link Uploaded Successfuly!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Intent i = new Intent(DriveActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DriveActivity.this, "Video Link Upload Failed!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Intent i = new Intent(DriveActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    });
                } else {
                    Toast.makeText(DriveActivity.this, "Please fill both the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
