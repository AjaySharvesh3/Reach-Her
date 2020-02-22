package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DriveActivity extends AppCompatActivity {

    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference rootReference, demoReference;

    private Button driveLinkUploadBtn;
    private EditText driveLinkEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        rootReference = FirebaseDatabase.getInstance().getReference();
        demoReference = rootReference.child("DriveLinks");
        demoReference = demoReference.child(user_id);

        driveLinkUploadBtn = findViewById(R.id.driveLinkUpload);
        driveLinkEditText = findViewById(R.id.driveLinkEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        driveLinkUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final String driveLink = driveLinkEditText.getText().toString();
            final String description = descriptionEditText.getText().toString();
            if (!TextUtils.isEmpty(driveLink) && !TextUtils.isEmpty(description)) {
                user_id = firebaseAuth.getCurrentUser().getUid();
                demoReference.child("desc").push().setValue(description);
                demoReference.child("link").push().setValue(driveLink);
            } else {
                Toast.makeText(DriveActivity.this, "Please fill both the fields", Toast.LENGTH_LONG).show();
            }
            }
        });
    }
}
