package com.majorproject.project.userauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.majorproject.project.MainActivity;
import com.majorproject.project.R;

public class UserLoginActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseUser currentuser;
    private Button loginbutton,phoneloginbutton;
    private EditText userEmail,userPassword;
    private TextView forgotPasswordLink,newAccountLink;
    private ProgressDialog loadingbar;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();




//        Log.d(UserLoginActivity.ACTIVITY_SERVICE,"current user = "+currentuser.toString());

        initializeFields();

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allowUserToLogin();
            }
        });





        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });
    }

    private void allowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("logging into account");
            loadingbar.setMessage("please wait.....");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        Toast.makeText(UserLoginActivity.this, "Logged in successful", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                        loadingbar.dismiss();

                    }else{
                        Toast.makeText(UserLoginActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }

    }



    private void initializeFields() {
        loginbutton = (Button) findViewById(R.id.login_button);
        phoneloginbutton = (Button) findViewById(R.id.login_using_phone);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        forgotPasswordLink = (TextView) findViewById(R.id.forgot_password);
        newAccountLink = (TextView) findViewById(R.id.new_user);
        loadingbar = new ProgressDialog(this);
    }



    private void sendUserToMainActivity() {
        Intent mainintent = new Intent(UserLoginActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
    }
    private void SendUserToRegisterActivity() {

        Intent registerintent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
        startActivity(registerintent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser != null){
            sendUserToMainActivity();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}
