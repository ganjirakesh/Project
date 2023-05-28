package com.majorproject.project.userauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.majorproject.project.MainActivity;
import com.majorproject.project.R;

public class UserRegisterActivity extends AppCompatActivity {
    private Button register_button;
    private EditText register_email,register_password;
    private TextView already_have_account_link;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);
        mAuth = FirebaseAuth.getInstance();
        rootref = (DatabaseReference) FirebaseDatabase.getInstance().getReference();
        initializeields();

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });

        already_have_account_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });
    }

    private void createNewAccount() {

        String email = register_email.getText().toString();
        String password = register_password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("creating new account");
            loadingbar.setMessage("please wait.....");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        rootref.child("Users").child(currentUserId).setValue("");

                        sendUserToMainActivity();
                        Toast.makeText(UserRegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }else{
                        Toast.makeText(UserRegisterActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }

    }

    private void sendUserToLoginActivity() {
        Intent loginintent = new Intent(UserRegisterActivity.this, UserLoginActivity.class);
        startActivity(loginintent);
    }

    private void sendUserToMainActivity() {
        Intent mainintent = new Intent(UserRegisterActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
    }

    private void initializeields() {
        register_button = (Button) findViewById(R.id.register_button);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        already_have_account_link = (TextView) findViewById(R.id.login_using);

        loadingbar = new ProgressDialog(this);
    }
}