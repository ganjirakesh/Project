package com.majorproject.project.mapmodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.R;

import java.io.IOException;
import java.util.List;

public class TripSettings extends AppCompatActivity {

    private EditText sourceText,destinationText;
    private Button setTrip;
    private String source,destination,Source,Destination,Source_Final,Destination_Final,currentGroupName;
    private DatabaseReference groupRef;
    double destinationLatitude,destinationLongitude,sourceLatitude,sourceLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_settings);
        sourceText = (EditText) findViewById(R.id.SOURCE);
        destinationText = (EditText) findViewById(R.id.DESTINATION);
        setTrip = (Button) findViewById(R.id.SET);
        currentGroupName = getIntent().getExtras().get("currentGroupName").toString();
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("Trip Coordinates");

        setTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                source = sourceText.getText().toString();
                destination = destinationText.getText().toString();
                Toast.makeText(TripSettings.this, source+" "+destination, Toast.LENGTH_SHORT).show();

                getSourceCoordinates(source);
                getDestinationCoordinates(destination);
                storeCoordinates();

            }
        });

        getValues();

    }

    private void getValues() {
        groupRef.child("Source").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String SourceName = snapshot.child("source").getValue().toString();
                    sourceText.setText(SourceName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        groupRef.child("Destination").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String DestinationName = snapshot.child("destination").getValue().toString();
                    destinationText.setText(DestinationName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeCoordinates() {
        groupRef.child("Source").child("source").setValue(source);
        groupRef.child("Source").child("latitude").setValue(sourceLatitude);
        groupRef.child("Source").child("longitude").setValue(sourceLongitude);
        groupRef.child("Destination").child("destination").setValue(destination);
        groupRef.child("Destination").child("latitude").setValue(destinationLatitude);
        groupRef.child("Destination").child("longitude").setValue(destinationLongitude).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(TripSettings.this, "Coordinates are set let go.......", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void getDestinationCoordinates(String destination) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(destination.toString(), 1);
            if (addressList != null) {

                destinationLatitude = addressList.get(0).getLatitude();
                destinationLongitude = addressList.get(0).getLongitude();
                System.out.println("destination coordinates are:" +destinationLatitude+" "+destinationLongitude);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getSourceCoordinates(String source) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(source.toString(), 1);
            if (addressList != null) {

                sourceLatitude = addressList.get(0).getLatitude();
                sourceLongitude = addressList.get(0).getLongitude();
                System.out.println("source coordinates are:" + sourceLatitude + " " +sourceLongitude);




            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}