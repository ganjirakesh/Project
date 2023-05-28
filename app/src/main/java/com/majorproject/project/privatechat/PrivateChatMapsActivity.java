package com.majorproject.project.privatechat;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.R;
import com.majorproject.project.databinding.ActivityPrivateChatMapsBinding;
import com.majorproject.project.mapmodule.MapsActivity;
import com.majorproject.project.mapmodule.MyLocation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PrivateChatMapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private DecimalFormat decfor = new DecimalFormat("0.00");
    private ActivityPrivateChatMapsBinding binding;
    private String SenderID,RecieverId,SenderName,RecieverName,currentUserId,A,B;
    private TextView DistanceMeasure;
    Handler handler;
    Runnable r;
    private Button distanceFinder;
    private FirebaseAuth mAuth;
    double PI_RAD = Math.PI/180.0;
    private LocationManager manager;
    private DatabaseReference reference,UserRef;
    private double SenderLatitude,SenderLongitude,RecieverLatitude,RecieverLongitude;
    Set<String> set,Names ;
    private ListView listViewGroups,names_list;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayAdapter<String> namesAdapter;
    private ArrayList<String> members = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private LatLng CurrentUserLatLng,RecieverLatLng;
    private Marker Sender,Reciever;
    private LatLng senderLat = new LatLng(SenderLatitude,SenderLongitude);
    private LatLng RecieverLat = new LatLng(RecieverLatitude,RecieverLongitude);
    private final int MIN_TIME = 1000;
    private final int MIN_DIST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivateChatMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mAuth=FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.PrivateMap);
        mapFragment.getMapAsync(this);
        SenderID = getIntent().getExtras().get("currentUser").toString();
        RecieverId = getIntent().getExtras().get("recieverId").toString();
        Log.d(PrivateChatMapsActivity.ACTIVITY_SERVICE,RecieverId);
        Log.d(PrivateChatMapsActivity.ACTIVITY_SERVICE,SenderID);
        initializeFields();
//        getSenderData(SenderID);
//        getRecieverData(RecieverId);
        getUsersUid(SenderID,RecieverId);
        getUserNames(SenderID);
        getUserNames(RecieverId);
        getLocationUpdates();
        readCurrentUserChanges();
        listViewGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int positionOfGroup, long GroupId) {
                String id = adapterView.getItemAtPosition(positionOfGroup).toString();

                if(id.equals(SenderID)){
                    Log.d(MapsActivity.ACTIVITY_SERVICE,id);
                    readCurrentUserChanges();
                }else{
                    Log.d(MapsActivity.ACTIVITY_SERVICE,id);
                    readChanges(id);
                }

            }


        });
        distanceFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findDistance();
            }
        });
    }
    private void readChanges(String uid) {

        reference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                marker.remove();
                if (snapshot.exists()) {
                    try {

                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if(location != null) {
                            RecieverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            Reciever.setPosition(RecieverLatLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(RecieverLatLng,15));
                            Reciever.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.groupuserlive));
                            Reciever.setTitle(RecieverName);


                        }
                    } catch (Exception e) {
                        Toast.makeText(PrivateChatMapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void readCurrentUserChanges() {
        reference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                marker.remove();
                if (snapshot.exists()) {
                    try {

                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if(location != null) {
                            CurrentUserLatLng= new LatLng(location.getLatitude(), location.getLongitude());

                            Sender.setPosition(CurrentUserLatLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentUserLatLng,18));
                            Sender.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.liveicon));
                            Sender.setTitle(SenderName);

                        }
                    } catch (Exception e) {
                        Toast.makeText(PrivateChatMapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserNames(String id) {

        reference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Names=new HashSet<>();
                if(snapshot.exists()){
                    String Name = snapshot.child("name").getValue().toString();
                    Names.add(Name);
                }

                names.addAll(Names);
                namesAdapter.notifyDataSetChanged();
                names.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getUsersUid(String senderID, String recieverId) {
        reference.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                set=new HashSet<>();
//                Names=new HashSet<>();

                if(snapshot.exists()){
                    SenderLatitude = (double) snapshot.child("latitude").getValue();
                    SenderLongitude  =(double) snapshot.child("longitude").getValue();
                    SenderName = snapshot.child("name").getValue().toString();
                    set.add(SenderID);
                    reference.child(recieverId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                RecieverLatitude = (double) snapshot.child("latitude").getValue();
                                RecieverLongitude  =(double) snapshot.child("longitude").getValue();
                                RecieverName = snapshot.child("name").getValue().toString();
                                set.add(RecieverId);
                            }
                            members.clear();
                            members.addAll(set);
                            arrayAdapter.notifyDataSetChanged();


                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                members.clear();
                members.addAll(set);
                arrayAdapter.notifyDataSetChanged();

//                names.clear();
//                names.addAll(Names);
//                namesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
//        recyclerView =(RecyclerView) findViewById(R.id.names_list);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewGroups = (ListView)findViewById(R.id.Private_Chat_list);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,members);
        listViewGroups.setAdapter(arrayAdapter);
        names_list = (ListView)findViewById(R.id.names_list);
        namesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,names);
        names_list.setAdapter(namesAdapter);
        distanceFinder =(Button) findViewById(R.id.distanceFinder);
       DistanceMeasure = findViewById(R.id.DistanceMeasure);//


    }






    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Sender=mMap.addMarker(new MarkerOptions().position(senderLat).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.liveicon)));

        Reciever=mMap.addMarker(new MarkerOptions().position(RecieverLat).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.groupuserlive)));


        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(senderLat,18));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RecieverLat,18));
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void getLocationUpdates() {
        if (manager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DIST,  this);
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



    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location!=null){
//            readChanges(UserId);
            saveLocation(location);
//            marker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));

        }else{
            Toast.makeText(this, "No location", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveLocation(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();


        UserRef.child(currentUserId).child("latitude").setValue(latitude);
        UserRef.child(currentUserId).child("longitude").setValue(longitude);


    }
    private boolean findDistance() {

        Location locationA = new Location(A);
        Location locationB = new Location(B);
        if(locationA.equals("") && locationB.equals("")){
            return false;
        }else{
//            readCurrentUserChanges();
//            readChanges(RecieverId);
            locationA.setLatitude(CurrentUserLatLng.latitude);
            locationA.setLongitude(CurrentUserLatLng.longitude);
            locationB.setLatitude(RecieverLatLng.latitude);
            locationB.setLongitude(RecieverLatLng.longitude);
            String res =decfor.format((locationA.distanceTo(locationB))/1000) + "km";
            DistanceMeasure.setText(res);
            handler = new Handler();

            r = new Runnable() {
                public void run() {

                    findDistance();

                }
            };
            handler.postDelayed(r, 2000);
            return true;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        getLocationUpdates();
//        getuserdata();
        readCurrentUserChanges();
//        getTripCoordinates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!findDistance()){
            handler.removeCallbacks(r);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!findDistance()) {
            handler.removeCallbacks(r);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        findDistance();

    }
}