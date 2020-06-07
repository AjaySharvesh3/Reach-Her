package com.example.chatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegisterDisplayName;
    private EditText mRegisterEmail;
    private EditText mRegisterPassword;
    private Button mRegisterUserButton;
    private FirebaseAuth mAuth;
    private Toolbar mRegisterToolbar;
    private ProgressDialog mregProgressDialog;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mRegisterDisplayName = findViewById(R.id.registerDisplayName);
        mRegisterEmail = findViewById(R.id.registerEmail);
        mRegisterPassword = findViewById(R.id.registerPassword);
        mRegisterUserButton = findViewById(R.id.registerCreateUserButton);
        mRegisterToolbar = findViewById(R.id.registerToolbar);

        setSupportActionBar(mRegisterToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mregProgressDialog = new ProgressDialog(this);

        mRegisterUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mRegisterDisplayName.getText().toString();
                String email = mRegisterEmail.getText().toString();
                String password = mRegisterPassword.getText().toString();
                if (!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mregProgressDialog.setTitle("Registering User");
                    mregProgressDialog.setMessage("Hold on while creating an account for you");
                    mregProgressDialog.show();
                    mregProgressDialog.setCanceledOnTouchOutside(false);
                    registerUserMethod(displayName, email, password);
                }
            }
        });
    }

    private void registerUserMethod(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){


                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("status", "Hi there I'm using Lapit Chat App.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mregProgressDialog.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }

                        }
                    });


                } else {

                    mregProgressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot Sign in. Please check the form and try again.", Toast.LENGTH_LONG).show();

                }

            }
        });
    }
}
