package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FileUploadActivity extends AppCompatActivity {

    ImageButton selectFile;
    Button upload;
    TextView notification;
    EditText titleEditText, descEditText;

    FirebaseStorage storage;
    FirebaseDatabase database;

    public Uri pdfUri;
    ProgressDialog progressDialog;
    private String user_id, name, phone;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference rootReference, demoReference, reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        rootReference = FirebaseDatabase.getInstance().getReference();
        demoReference = rootReference.child("UploadedFiles");

        selectFile = findViewById(R.id.drive_link_btn);
        upload = findViewById(R.id.fileUpload);
        notification = findViewById(R.id.filePathTv);
        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descEditText);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        name = task.getResult().getString("name");
                        phone = task.getResult().getString("phone");
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(FileUploadActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(FileUploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdfFile();
                } else {
                    ActivityCompat.requestPermissions(FileUploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri != null) {
                    uploadFile(pdfUri);
                    final String title = titleEditText.getText().toString();
                    final String description = descEditText.getText().toString();
                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
                        user_id = firebaseAuth.getCurrentUser().getUid();
                        reference = demoReference.child(name + "_" + phone).push();
                        reference.child("title").setValue(title);
                        reference.child("desc").setValue(description);
                    } else {
                        Toast.makeText(FileUploadActivity.this, "Please fill both the fields", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(FileUploadActivity.this, "Please select a file...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadFile(Uri uri) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading your file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String title = titleEditText.getText().toString();
        final String filename = title + "";
        StorageReference storageReference = storage.getReference();
        storageReference.child("files").child(name + "_" + phone).child(filename).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = taskSnapshot.getMetadata().toString();
                        reference.child("filename").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(FileUploadActivity.this, "File uploaded successfully", Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                    Intent i = new Intent(FileUploadActivity.this, MainActivity.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(FileUploadActivity.this, "File upload filed", Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FileUploadActivity.this, "File uploaded successfully", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdfFile();
        } else {
            Toast.makeText(FileUploadActivity.this, "Please provide permission to read your file...", Toast.LENGTH_LONG).show();
        }
    }

    private void selectPdfFile() {
        Intent i = new Intent();
        i.setType("application/pdf");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, 86);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            notification.setText("File Path: " + pdfUri.getLastPathSegment());
        } else {
            Toast.makeText(FileUploadActivity.this, "Please select a file", Toast.LENGTH_LONG).show();
        }
    }
}
