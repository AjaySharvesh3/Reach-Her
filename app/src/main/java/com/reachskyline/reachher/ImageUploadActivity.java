package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class ImageUploadActivity extends AppCompatActivity {

    private EditText titleEditText, descEditText;
    ImageView choseImageView;
    ImageButton choseImageButton;
    Button uploadImage;
    StorageReference mStorageRef;
    public Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference reference, demoReference, rootReference;
    private String user_id, username, phone;
    private FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        mStorageRef = FirebaseStorage.getInstance().getReference("images");
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        rootReference = FirebaseDatabase.getInstance().getReference();
        demoReference = rootReference.child("UploadedImages");

        choseImageView = findViewById(R.id.choseImageView);
        choseImageButton = findViewById(R.id.choseImageButton);
        uploadImage = findViewById(R.id.imageUpload);
        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descEditText);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        username = task.getResult().getString("name");
                        phone = task.getResult().getString("phone");
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(ImageUploadActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        Log.i("hiiiiiii_imageUploadActivity", "" + username + " " + phone);

        choseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((TextUtils.isEmpty(titleEditText.getText().toString())) && (imageUri == null)) {
                    Toast.makeText(ImageUploadActivity.this, "Please choose your image to upload and Add your Image Name", Toast.LENGTH_LONG).show();
                    return;
                }
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(ImageUploadActivity.this, "Upload in progress...", Toast.LENGTH_LONG).show();
                } else {
                    fileUploader();
                }
            }
        });
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void fileUploader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading your file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        StorageReference ref = mStorageRef.child(username + "_" + phone).child(username + "_" + titleEditText.getText().toString()+ "." + getExtension(imageUri));
        uploadTask = ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri url = uri.getResult();
                final String title = titleEditText.getText().toString();
                final String description = descEditText.getText().toString();
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
                    user_id = firebaseAuth.getCurrentUser().getUid();
                    reference = demoReference.child(username + "_" + phone).push();
                    reference.child("title").setValue(title);
                    reference.child("desc").setValue(description).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ImageUploadActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Intent i = new Intent(ImageUploadActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ImageUploadActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(ImageUploadActivity.this, "Please fill both the fields", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageUploadActivity.this, "Please choose your image to upload", Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    private void fileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            choseImageView.setImageURI(imageUri);
        }
    }
}
