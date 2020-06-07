package com.example.chatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;
    private ProgressDialog progressDialog;

    private DatabaseReference mStatusDatabse;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();
        mStatusDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);


        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = findViewById(R.id.status_input);
        mSaveBtn = findViewById(R.id.status_save_btn);

        String statusValue = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(statusValue);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please hang on, we are trying to save your status.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                String status = mStatus.getEditText().getText().toString();
                mStatusDatabse.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(StatusActivity.this, "Status Uploaded..! Go back to profile and check it out.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(StatusActivity.this, "There is an issue in saving your status..!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
