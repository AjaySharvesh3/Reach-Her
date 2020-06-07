package com.reachskyline.reachher;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.security.acl.Group;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserID;
    private ImageButton driveLinkBtn, imageBtn, fileBtn;

    private ImageView imageView1, imageView2, imageView3, imageView4;
    private TextView imag1, imag2, imag3, imag4;

    private TextView helloMessageTv;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        /*helloMessageTv = findViewById(R.id.welcome_message);*/
        driveLinkBtn = findViewById(R.id.drive_link_btn);
        imageBtn = findViewById(R.id.imageBtn);
        fileBtn = findViewById(R.id.fileBtn);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);

        imag1 = findViewById(R.id.img1);
        imag2 = findViewById(R.id.img2);
        imag3 = findViewById(R.id.img3);
        imag4 = findViewById(R.id.img4);

        DocumentReference user = firebaseFirestore.collection("FILES").document("poster_images");
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    StringBuilder img1 = new StringBuilder("");
                    img1.append(doc.get("image1"));
                    imag1.setText(img1.toString());
                    String imageUrl = imag1.getText().toString();
                    Picasso.get().load(imageUrl).placeholder(R.drawable.image_placeholder).into(imageView1);

                    StringBuilder img2 = new StringBuilder("");
                    img2.append(doc.get("image2"));
                    imag2.setText(img2.toString());
                    String imageUrl2 = imag2.getText().toString();
                    Picasso.get().load(imageUrl2).placeholder(R.drawable.image_placeholder).into(imageView2);

                    StringBuilder img3 = new StringBuilder("");
                    img3.append(doc.get("image3"));
                    imag3.setText(img1.toString());
                    String imageUrl3 = imag3.getText().toString();
                    Picasso.get().load(imageUrl3).placeholder(R.drawable.image_placeholder).into(imageView3);

                    StringBuilder img4 = new StringBuilder("");
                    img4.append(doc.get("image4"));
                    imag4.setText(img4.toString());
                    String imageUrl4 = imag4.getText().toString();
                    Picasso.get().load(imageUrl4).placeholder(R.drawable.image_placeholder).into(imageView4);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Due to low internet connectivity, banner image is not loading", Toast.LENGTH_SHORT).show();
            }
        });

        driveLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DriveActivity.class);
                startActivity(i);
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ImageUploadActivity.class);
                startActivity(i);
            }
        });

        fileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, FileUploadActivity.class);
                startActivity(i);
            }
        });

        /*Intent i = getIntent();
        String username = i.getStringExtra("name");
        String message = "Hello " + username + ", Welcome to Reach Her Digital";
        helloMessageTv.setText(message);*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser ==  null) {
            sentToLogin();
        } else {
            currentUserID = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent setUpIntent = new Intent(MainActivity.this, SetUpActivity.class);
                            startActivity(setUpIntent);
                            finish();
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu item) {
        getMenuInflater().inflate(R.menu.main_menu, item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout_button:
                logout();
                return true;
            case R.id.drive_history_button:
                Intent i = new Intent(MainActivity.this, FetchDataActivity.class);
                startActivity(i);
                return true;
            case R.id.image_history_button:
                Intent i1 = new Intent(MainActivity.this, FetchImageDataActivity.class);
                startActivity(i1);
                return true;
            case R.id.file_history_button:
                Intent i2 = new Intent(MainActivity.this, FetchFilesDataActivity.class);
                startActivity(i2);
                return true;
            case R.id.group_chat_button:
                Intent i3 = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(i3);
                return true;
            case R.id.action_settings_button:
                Intent settingsIntent = new Intent(MainActivity.this, SetUpActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return true;
        }
    }

    private void logout() {
        mAuth.signOut();
        sentToLogin();
    }

    private void sentToLogin() {
        Intent loginIntent  = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

}
