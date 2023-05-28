package com.majorproject.project.mapmodule;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.location.LocationListener;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.Contacts;
import com.majorproject.project.ContactsFragment;
import com.majorproject.project.FindFriends;
import com.majorproject.project.GroupUsersList;
import com.majorproject.project.MainActivity;
import com.majorproject.project.ProfileActivity;
import com.majorproject.project.R;
import com.majorproject.project.databinding.ActivityMapsBinding;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_MAGENTA;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{

    private GoogleMap mMap;
    private DecimalFormat decfor = new DecimalFormat("0.00");
    Handler handler;
    Runnable r;
    String Name,UserId,user;
    String A,B;
    Button distanceFinder;
    SupportMapFragment mapFragment;
    private DatabaseReference db,usersRef,UserRef,grpUsersRef,tripCoordinatesRef;
    private com.majorproject.project.databinding.ActivityMapsBinding binding;
    private LocationManager manager;
    private final int MIN_TIME = 1000;
    private final int MIN_DIST = 1;
    private String currentUser,currentGroupName,currentUserName,friendUser;
    private FirebaseAuth mAuth;
    private Marker marker,usermark;
    private ListView listViewGroups;
    private ArrayAdapter<String> arrayAdapter;
    MarkerOptions markerOptions;
    private ArrayList<String> members = new ArrayList<>();
    private RecyclerView recyclerView;

    LatLng origin = new LatLng(17.274978, 78.548781);
    TextView DistanceMeasure;
    String currid;
    LatLng currentuserlatlng,groupuser,sourceLatLng,destinationLatLng;
    double PI_RAD = Math.PI/180.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);


        mAuth = FirebaseAuth.getInstance();


        currentUser = getIntent().getExtras().get("currentUser").toString();
//        friendUser = getIntent().getExtras().get("recieverId").toString();
        currentGroupName = getIntent().getExtras().get("currentGroupName").toString();
        currentUserName = getIntent().getExtras().get("currentUserName").toString();
        grpUsersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("users").child(currentUser);
        tripCoordinatesRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("Trip Coordinates");


        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);

        usersRef =FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("users");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        initializeFields();
        getuserdata();
        getLocationUpdates();
        readCurrentUserChanges();
        getUserNames();



        listViewGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int positionOfGroup, long GroupId) {
                currid = adapterView.getItemAtPosition(positionOfGroup).toString();

                if(currid.equals(currentUser)){
                    Log.d(MapsActivity.ACTIVITY_SERVICE,currid);

                    readCurrentUserChanges();




                }else{
                    Log.d(MapsActivity.ACTIVITY_SERVICE,currid);

                    readChanges(currid);




                }

            }


        });
        distanceFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findDistance();
            }
        });

//        getNames();

    }




    private void getuserdata() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();

                Iterator iterator = snapshot.getChildren().iterator();

                while(iterator.hasNext()) {

                  UserId = ((DataSnapshot) iterator.next()).getKey();
                  UserRef.child(UserId).addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot snapshot) {
                              if (snapshot.exists()) {
//                              String longitude = snapshot.child("latitude").getValue().toString();
//                              String latitude = snapshot.child("longitude").getValue().toString();
                                  Name = snapshot.child("name").getValue().toString();
//                              LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//                              createMarker(latLng, Name, 0);
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError error) {

                          }
                      });

                    set.add(UserId);
//                    nameSet.add(Name);

                }
                members.clear();
//                names.clear();
                members.addAll(set);
//                names.addAll(nameSet);
                arrayAdapter.notifyDataSetChanged();
//                nameadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    }

    private void readCurrentUserChanges() {
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                marker.remove();
                if (snapshot.exists()) {
                    try {

                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if(location != null) {
                            currentuserlatlng = new LatLng(location.getLatitude(), location.getLongitude());

                            marker.setPosition(currentuserlatlng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentuserlatlng,18));
                            marker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.liveicon));
                            marker.setTitle(currentUserName);

                        }
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
        recyclerView =(RecyclerView) findViewById(R.id.names_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listViewGroups = (ListView)findViewById(R.id.group_list);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,members);
        listViewGroups.setAdapter(arrayAdapter);
        distanceFinder =(Button) findViewById(R.id.distanceFinder);
        DistanceMeasure = findViewById(R.id.DistanceMeasure);


    }
//    private void getusedata() {
//        usersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Set<String> set = new HashSet<>();
//                Iterator iterator = snapshot.getChildren().iterator();
//
//                while(iterator.hasNext()) {
//                    String uid = ((DataSnapshot) iterator.next()).getKey();
//                    assert uid != null;
//                    UserRef.child(uid).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String longitude = snapshot.child("latitude").getValue().toString();
//                            String latitude = snapshot.child("longitude").getValue().toString();
//                            Name = snapshot.child("name").getValue().toString();
//                            Log.d(MapsActivity.ACTIVITY_SERVICE,Name);
//
//                            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
////                            createMarker(latLng, Name, 1);
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    set.add(Name);
//                }
//                members.clear();
//                members.addAll(set);
//                arrayAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }



//    private String getMemberLocationInformation(String userid) {
//        UserRef.child(userid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String longitude = snapshot.child("latitude").getValue().toString();
//                String latitude = snapshot.child("longitude").getValue().toString();
//                Name = snapshot.child("name").getValue().toString();
//
//                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//                createMarker(latLng, Name, 1);
//
//
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        return Name;
//    }


    private void createMarker(LatLng latLng, String name, int type) {
        markerOptions = new MarkerOptions();
        if(type==0) {
            markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.online)); // Users
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(HUE_RED)); // Locations
        }
        markerOptions.position(latLng);
        markerOptions.title(name);
//        marker.setPosition(latLng);

    }


    private void readChanges(String uid) {

        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                marker.remove();
                if (snapshot.exists()) {
                    try {

                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if(location != null) {
                            groupuser = new LatLng(location.getLatitude(), location.getLongitude());

                            usermark.setPosition(groupuser);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(groupuser,16));
                            usermark.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.groupuserlive));
                            UserRef.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                         user = snapshot.child("name").getValue().toString();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            usermark.setTitle(user);


                        }
                    } catch (Exception e) {
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        marker=mMap.addMarker(new MarkerOptions().position(origin).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.liveicon))
                );
        usermark=mMap.addMarker(new MarkerOptions().position(origin).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.groupuserlive))
        );

//        mMap.setMinZoomPreference(20);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin,10));


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

        db.child("latitude").setValue(latitude);
        db.child("longitude").setValue(longitude);
        grpUsersRef.child("latitude").setValue(latitude);
        grpUsersRef.child("longitude").setValue(longitude);
        grpUsersRef.child("name").setValue(currentUserName);




    }
    private boolean findDistance() {

        Location locationA = new Location(A);
        Location locationB = new Location(B);
        if(locationA.equals("") && locationB.equals("")){
            return false;
        }else{
            readCurrentUserChanges();
            readChanges(currid);
            locationA.setLatitude(currentuserlatlng.latitude);
            locationA.setLongitude(currentuserlatlng.longitude);
            locationB.setLatitude(groupuser.latitude);
            locationB.setLongitude(groupuser.longitude);
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








//    @Override
//    protected void onStart() {
//        super.onStart();
//        getLocationUpdates();
//        readCurrentUserChanges();
//
//
//
//    }


    @Override
    protected void onStart() {
        super.onStart();
        getLocationUpdates();
        getuserdata();
        readCurrentUserChanges();
        getTripCoordinates();

    }




    private void getTripCoordinates() {
        tripCoordinatesRef.child("Source").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MyLocation location = snapshot.getValue(MyLocation.class);
                    if (location != null) {
                        String sourceLocation = snapshot.child("source").getValue().toString();
                        sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(sourceLatLng).title(sourceLocation).icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sourceLatLng,10));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        tripCoordinatesRef.child("Destination").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MyLocation location = snapshot.getValue(MyLocation.class);
                    if (location != null) {
                        String destinationLocation = snapshot.child("destination").getValue().toString();
                        destinationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationLocation).icon(BitmapDescriptorFactory.defaultMarker(HUE_MAGENTA)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng,10));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserNames() {
        FirebaseRecyclerOptions<GroupUsersList> options = new FirebaseRecyclerOptions.Builder<GroupUsersList>()
                .setQuery(usersRef,GroupUsersList.class)
                .build();

        FirebaseRecyclerAdapter<GroupUsersList,GroupUserViewHolder> adapter = new FirebaseRecyclerAdapter<GroupUsersList, GroupUserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupUserViewHolder holder, int position, @NonNull GroupUsersList model) {
                holder.grp_user_name.setText(model.getNAME());


//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String visit_user_id = getRef(position).getKey();
//                        Intent profileIntent = new Intent(FindFriends.this, ProfileActivity.class);
//                        profileIntent.putExtra("visit_user_id",visit_user_id);
//                        startActivity(profileIntent);
//                    }
//                });
            }

            @NonNull
            @Override
            public GroupUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_user,parent,false);
                GroupUserViewHolder viewHolder = new GroupUserViewHolder(v);
                return  viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupUserViewHolder extends RecyclerView.ViewHolder {
        TextView grp_user_name;
        public GroupUserViewHolder(@NonNull View itemView) {
            super(itemView);
            grp_user_name = itemView.findViewById(R.id.Group_user_name);


        }
    }








}