package com.reachskyline.reachher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;

    private String user_id, name, phone, bio, strength, location;

    private boolean isChanged = false;

    private EditText setupName, setUpPhone, setUpBio, setUpStrength, setUpLocation;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setUpPhone = findViewById(R.id.setup_phone);
        setUpBio = findViewById(R.id.setup_bio);
        setUpStrength = findViewById(R.id.setup_strength);
        setUpLocation = findViewById(R.id.setup_location);
        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progress);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){

                        name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        phone = task.getResult().getString("phone");
                        bio = task.getResult().getString("bio");
                        strength = task.getResult().getString("core_strength");
                        location = task.getResult().getString("location");

                        mainImageURI = Uri.parse(image);

                        setupName.setText(name);
                        setUpPhone.setText(phone);
                        setUpBio.setText(bio);
                        setUpStrength.setText(strength);
                        setUpLocation.setText(location);
                        setupName.setFocusable(false);
                        setupName.setClickable(false);

                        setUpPhone.setFocusable(false);
                        setUpPhone.setClickable(false);

                        setUpBio.setFocusable(false);
                        setUpBio.setClickable(false);

                        setUpStrength.setFocusable(false);
                        setUpStrength.setClickable(false);

                        setUpLocation.setFocusable(false);
                        setUpLocation.setClickable(false);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetUpActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);


                    }

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetUpActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setupName.getText().toString();
                final String phone_number = setUpPhone.getText().toString();
                final String bio = setUpBio.getText().toString();
                final String strength = setUpStrength.getText().toString();
                final String location = setUpLocation.getText().toString();

                if (!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(phone_number) && !TextUtils.isEmpty(bio)
                        && !TextUtils.isEmpty(strength) && !TextUtils.isEmpty(location) && mainImageURI != null) {

                    setupProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        File newImageFile = new File(mainImageURI.getPath());
                        try {

                            compressedImageFile = new Compressor(SetUpActivity.this)
                                    .setMaxHeight(125)
                                    .setMaxWidth(125)
                                    .setQuality(50)
                                    .compressToBitmap(newImageFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask image_path = storageReference.child("profile_images").child(name + "_" + phone).child(name + "_" + user_id + ".jpg").putBytes(thumbData);

                        image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, user_name, phone_number, bio, strength, location);

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetUpActivity.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();

                                    setupProgress.setVisibility(View.INVISIBLE);

                                }
                            }
                        });

                    } else {

                        storeFirestore(null, user_name, phone_number, bio, strength, location);

                    }

                }

            }

        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetUpActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        BringImagePicker();
                    }
                } else {
                    BringImagePicker();
                }
            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, final String user_name, final String phone_number, final String bioInfo, final String strengthInfo, final String locationInfo) {
       task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
               while(!uri.isComplete());
               Uri url = uri.getResult();

               final Map<String, String> userMap = new HashMap<>();
               userMap.put("name", user_name);
               userMap.put("image", url.toString());
               userMap.put("phone", phone_number);
               userMap.put("bio", bioInfo);
               userMap.put("core_strength", strengthInfo);
               userMap.put("location", locationInfo);

               firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {

                       if(task.isSuccessful()){

                           Toast.makeText(SetUpActivity.this, "The user settings are updated.", Toast.LENGTH_LONG).show();
                           Intent mainIntent = new Intent(SetUpActivity.this, MainActivity.class);
                           mainIntent.putExtra("name", userMap.get("name"));
                           startActivity(mainIntent);
                           finish();

                       } else {

                           String error = task.getException().getMessage();
                           Toast.makeText(SetUpActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                       }

                       setupProgress.setVisibility(View.INVISIBLE);

                   }
               });
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(SetUpActivity.this, "Upload Error: " +
                       e.getMessage(), Toast.LENGTH_LONG).show();
           }
       });
    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetUpActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
