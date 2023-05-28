package com.majorproject.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.userauthentication.UserLoginActivity;

import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements LocationListener {
    private Button updateAccountSettings;
    private EditText username,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference rootref;
    Toolbar toolbar;
    private LocationManager manager;
    private final int MIN_TIME = 1000;
    private final int MIN_DIST = 1;
    private static final int GALLERYPICK = 1;
    Double latitude,longitude;
    private String groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth= FirebaseAuth.getInstance();
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        toolbar = (Toolbar) findViewById(R.id.setting_bar_layout);

        currentUserId = mAuth.getCurrentUser().getUid();

        rootref = FirebaseDatabase.getInstance().getReference();
        initializeFields();
        getLocationUpdates();
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upadteprofile();
            }
        });

        retrieveUserdata();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERYPICK);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

    }
    private void getLocationUpdates() {
        if (manager != null) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DIST,this);
                } else {
                    Toast.makeText(this, "No provider", Toast.LENGTH_SHORT).show();
                }


            }else{
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},101);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }else{
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }

        }
    }
    public void onLocationChanged(@NonNull Location location) {
        if(location!=null){
//            readChanges(UserId);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }else{
            Toast.makeText(this, "No location", Toast.LENGTH_SHORT).show();
        }
    }


    private void upadteprofile() {

        String name = username.getText().toString();
        String status = userStatus.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "field cannot be empty", Toast.LENGTH_SHORT).show();
            username.requestFocus();
        }
        if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "field cannot be empty", Toast.LENGTH_SHORT).show();
            userStatus.requestFocus();
        }else{
            loadingbar.setTitle("Profile is updating");
            loadingbar.setMessage("please wait.....");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",name);
            profileMap.put("status",status);

            rootref.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        Toast.makeText(SettingsActivity.this, "profile updated successfully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                        sendUserToMainActivity();


                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
            rootref.child("Users").child(currentUserId).child("latitude").setValue(latitude);
            rootref.child("Users").child(currentUserId).child("longitude").setValue(longitude);


        }
    }

    private void initializeFields() {
        updateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.profile_image) ;

        loadingbar = new ProgressDialog(this);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == GALLERYPICK && resultCode == RESULT_OK && data!=null){
//            Uri ImageUri = data.getData();
//
//
//    }

    private void sendUserToMainActivity() {
        Intent mainintent = new Intent(SettingsActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
    }

    private void retrieveUserdata() {
        rootref.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))){

                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();

                    username.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);


                }else if((snapshot.exists()) && (snapshot.hasChild("name"))){
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveUserStatus = snapshot.child("status").getValue().toString();

                    username.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);

                }else{
                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "please set and update your profile ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}