package com.majorproject.project.chatmodule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.MainActivity;
import com.majorproject.project.R;
import com.majorproject.project.mapmodule.MapsActivity;
import com.majorproject.project.mapmodule.TripSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupChatActivity<latitude, longitude> extends AppCompatActivity {
    private Toolbar toolbar;
    private Button sendMessageButton;
    private EditText sendmessagetext;
    private ScrollView scrollView;
    private TextView displaytextmessages;

    private String currentGroupName,currentUser,currentUserName,currentDate,currentTime;
    private Double latitude=17.2345,longitude=78.5634;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,groupNameref,groupMessageKeyRef,messageKey,userkeyRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName = getIntent().getExtras().get("GroupName").toString();

        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameref = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        currentUser = mAuth.getCurrentUser().getUid();

        initializeFields();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        getuserInfo();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeMessagesToDatabase();
                sendmessagetext.setText("");

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.map_menu,menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.show_map){
//            mAuth.signOut();
            openMapActivity();
        }
        if(item.getItemId() == R.id.set_coordinates){
//            mAuth.signOut();
            OpenTripSettings();
        }

        return true;
    }

    private void OpenTripSettings() {
        Intent tripintent = new Intent(GroupChatActivity.this, TripSettings.class);
        tripintent.putExtra("currentGroupName",currentGroupName);
//        mapintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(tripintent);
//        finish();
    }


    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        sendMessageButton = (Button) findViewById(R.id.sendgroupMessage);
        sendmessagetext = (EditText) findViewById(R.id.input_group_message);
        scrollView = (ScrollView) findViewById(R.id.my_scrool_view);
        displaytextmessages = (TextView) findViewById(R.id.group_chat_text_display);

    }

    private void getuserInfo() {
        usersRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child("name").getValue().toString();
//                    latitude = Double.valueOf(snapshot.child("latitude").getValue().toString());
//                    longitude = Double.valueOf(snapshot.child("longitude").getValue().toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void openMapActivity() {
        Intent mapintent = new Intent(GroupChatActivity.this, MapsActivity.class);
        mapintent.putExtra("currentUser",currentUser);
        mapintent.putExtra("currentGroupName",currentGroupName);
        mapintent.putExtra("currentUserName",currentUserName);
//        mapintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapintent);
//        finish();
    }

    private void storeMessagesToDatabase() {
        String message = sendmessagetext.getText().toString();
        DatabaseReference users = groupNameref.child("users");
        String messageKey = groupNameref.push().getKey();
        String userKey = groupNameref.push().getKey();


        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "please enter message first.....", Toast.LENGTH_SHORT).show();
        }else{

            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, YYYY");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

//            users.child(currentUser).setValue(currentUserName);

//
            HashMap<String,Object> userskey = new HashMap<>();
            groupNameref.updateChildren(userskey);
//
            userkeyRef = users.child(currentUser);

            HashMap<String,Object> usersLocation = new HashMap<>();
            usersLocation.put("latitude",latitude);
            usersLocation.put("longitude",longitude);
            usersLocation.put("name",currentUserName);
            userkeyRef.updateChildren(usersLocation);

            HashMap<String,Object> groupMessagekey = new HashMap<>();
            groupNameref.updateChildren(groupMessagekey);



            groupMessageKeyRef = groupNameref.child("messages").child(messageKey);


            HashMap<String,Object> messageInfoMap = new HashMap<>();

                messageInfoMap.put("name",currentUserName);
                messageInfoMap.put("message",message);
                messageInfoMap.put("currentDate",currentDate);
                messageInfoMap.put("currentTime",currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);









        }
    }
//    private void requestTripLocations() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialog);
//        builder.setTitle("Enter Source and Destination");
//        final EditText source = new EditText(getApplicationContext());
//        final EditText destination = new EditText(getApplicationContext());
//        source.setHint("eg. hyderabad");
//        destination.setHint("eg. goa");
//        builder.setView(source);
//        builder.setView(destination);
//        builder.setPositiveButton("set", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                String SourceAddress = source.getText().toString();
//                String DestinationAddress = destination.getText().toString();
//                if(TextUtils.isEmpty(SourceAddress) || TextUtils.isEmpty(DestinationAddress) ){
//                    Toast.makeText(MapsActivity.this, "Field cannot be empty ", Toast.LENGTH_SHORT).show();
//                }else{
//                    SetCoordinates(SourceAddress,DestinationAddress);
//                }
//            }
//        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.cancel();
//            }
//        });
//        builder.show();
//
//
//    }

//    private void SetCoordinates(String source, String destination) {
//        rootref.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                    Toast.makeText(MainActivity.this, groupName+" Group is created successfull", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        });
//    }

    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
        while( iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            String chatmessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();

            displaytextmessages.append(chatName+" :\n"+chatmessage+"\n"+chatTime+"  "+chatDate+"\n\n\n");
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            },1000);
        }
    }
    @Override
    protected void onStart() {

        super.onStart();
        groupNameref.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}