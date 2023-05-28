package com.majorproject.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String recieveruserid,current_Stat,senderUserId;
    private CircleImageView userProfileImage;
    private TextView visitUserName,visitUserStatus;
    private Button visitUserSendMessage,declineRequestBtn;
    private DatabaseReference visitUserRef,chatRequestRef,contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth=FirebaseAuth.getInstance();
        visitUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef =FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        recieveruserid = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        visitUserName = (TextView) findViewById(R.id.visit_user_profile_name);
        visitUserStatus = (TextView) findViewById(R.id.visit_user_profile_status);
        visitUserSendMessage = (Button) findViewById(R.id.visit_send_message_btn);
        declineRequestBtn =(Button) findViewById(R.id.decline_send_message_btn);
        current_Stat = "new";
        retrieveUserInfo();

    }

    private void retrieveUserInfo() {
        visitUserRef.child(recieveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && (snapshot.hasChild("image"))){

//                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
//                    Picasso.get().load(userImage).placeholder(R.drawable.img).into(userProfileImage);
                    visitUserName.setText(userName);
                    visitUserStatus.setText(userStatus);
                    ManageChatRequest();
                }
                else{

                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
//
                    visitUserName.setText(userName);
                    visitUserStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(recieveruserid)){
                        String requestType = snapshot.child(recieveruserid).child("request_type").getValue().toString();
                        if(requestType.equals("sent")){
                            current_Stat="request_sent";
                            visitUserSendMessage.setText("cancel request");
                        }else if (requestType.equals("recieved")){
                            current_Stat="request_recieved";
                            visitUserSendMessage.setText("Accept Request");
                            declineRequestBtn.setVisibility(View.VISIBLE);
                            declineRequestBtn.setText("Decline Request");
                            declineRequestBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cancelChatRequest();
                                }
                            });

                        }
                    }else {
                        contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(recieveruserid)){
                                    current_Stat="friends";
                                    visitUserSendMessage.setText("Remove contact");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(!senderUserId.equals(recieveruserid)){
            visitUserSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    visitUserSendMessage.setEnabled(false);
                    if(current_Stat.equals("new")){
                        sendChatRequest();
                    }
                    if(current_Stat.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if(current_Stat.equals("request_recieved")){
                        AcceptChatRequest();
                    }
                    if(current_Stat.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });

        }else{
            visitUserSendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(recieveruserid).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                visitUserSendMessage.setEnabled(true);
                                current_Stat="new";
                                visitUserSendMessage.setText("Send Message");
                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                declineRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        contactsRef.child(senderUserId).child(recieveruserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsRef.child(recieveruserid).child(senderUserId).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                    chatRequestRef.child(senderUserId).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            chatRequestRef.child(recieveruserid).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                        visitUserSendMessage.setEnabled(true);
                                                        current_Stat="friends";
                                                        visitUserSendMessage.setText("Remove contact");

                                                        declineRequestBtn.setVisibility(View.INVISIBLE);
                                                        declineRequestBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                            }
                        }
                    });
                }
            }
        });

    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(recieveruserid).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                visitUserSendMessage.setEnabled(true);
                                current_Stat="new";
                                visitUserSendMessage.setText("Send Message");
                                declineRequestBtn.setVisibility(View.INVISIBLE);
                                declineRequestBtn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(recieveruserid).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(recieveruserid).child(senderUserId).child("request_type").setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                visitUserSendMessage.setEnabled(true);
                                current_Stat = "request_sent";
                                visitUserSendMessage.setText("cancel request");
                            }
                        }
                    });
                }
            }
        });
    }
}