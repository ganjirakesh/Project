package com.majorproject.project.privatechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.majorproject.project.R;
import com.majorproject.project.mapmodule.MapsActivity;
import com.majorproject.project.mapmodule.TripSettings;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String messageRecieverId,messageRecieverName,msgSenderId;
    private TextView privateChatUserName,privateChatLastSeen;
    private Button private_chat_input_send;
    private androidx.appcompat.widget.Toolbar toolbar;
    private EditText private_chat_input_message;
    private FirebaseAuth mAuth;
    private DatabaseReference userMessageKeyRef,RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        messageRecieverId = getIntent().getExtras().get("friendsIds").toString();
        messageRecieverName=getIntent().getExtras().get("friendName").toString();
        initializeFields();

        privateChatUserName.setText(messageRecieverName);
        private_chat_input_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(ChatActivity.this, "storing", Toast.LENGTH_SHORT).show();
                storeMessagesToDatabase();
                private_chat_input_message.setText("");
            }
        });
    }

    private void storeMessagesToDatabase() {
        String messageText = private_chat_input_message.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "please enter something", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef="Messages/"+msgSenderId+"/"+messageRecieverId;
            String messageRecieverRef = "Messages/"+messageRecieverId+"/"+msgSenderId;

            userMessageKeyRef= RootRef.child("Private Chat").child(msgSenderId).child(messageRecieverId).push();

            String MessagePushId = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",msgSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+MessagePushId,messageTextBody);
            messageBodyDetails.put(messageRecieverRef+"/"+MessagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "message sent failed", Toast.LENGTH_SHORT).show();

                    }
                    private_chat_input_message.setText("");
                }
            });


        }
    }

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
        Intent tripintent = new Intent(ChatActivity.this, TripSettings.class);
//        mapintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(tripintent);
//        finish();
    }
    private void openMapActivity() {
        Intent mapintent = new Intent(ChatActivity.this, PrivateChatMapsActivity.class);
        mapintent.putExtra("currentUser",msgSenderId);
        mapintent.putExtra("recieverId",messageRecieverId);
   //        mapintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapintent);
//        finish();
    }
    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(toolbar);
        private_chat_input_send = (Button) findViewById(R.id.private_chat_input_send);
        private_chat_input_message = (EditText)findViewById(R.id.private_chat_input_message);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ActionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(ActionBarView);

        privateChatUserName = (TextView) findViewById(R.id.private_chat_profile_name);
        privateChatLastSeen =(TextView) findViewById(R.id.private_chat_lase_seen);


    }
}