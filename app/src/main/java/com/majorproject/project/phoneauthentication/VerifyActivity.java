package com.majorproject.project.phoneauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.majorproject.project.MainActivity;
import com.majorproject.project.R;

public class VerifyActivity extends AppCompatActivity {


    private EditText verificationCodeEditText;
    private Button verifyButton;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        verificationCodeEditText = findViewById(R.id.OTP);
        verifyButton = findViewById(R.id.Verify);

        verificationId = getIntent().getStringExtra("verificationId");

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verificationCodeEditText.getText().toString().trim().isEmpty()){
                    Toast.makeText(VerifyActivity.this, "enter otp", Toast.LENGTH_SHORT).show();
                }
                else {
                    String verificationCode = verificationCodeEditText.getText().toString().trim();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                    FirebaseAuth.getInstance()
                            .signInWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(VerifyActivity.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }else{
                                Toast.makeText(VerifyActivity.this, "otp is not valid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }



}