package com.majorproject.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.majorproject.project.userauthentication.UserLoginActivity;

public class MainActivity extends AppCompatActivity {
    private Toolbar  mtoolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAdaptor tabsAdaptor;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private String groupName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        rootref = FirebaseDatabase.getInstance().getReference();
        Log.d(MainActivity.ACTIVITY_SERVICE,"current user="+user);

        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Triplet");

        viewPager = (ViewPager) findViewById(R.id.main_tabs_pagers);
        tabsAdaptor = new TabsAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdaptor);

        tabLayout=(TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);



    }



    private void SendUserToLoginActivity() {
        Intent loginintent = new Intent(MainActivity.this, UserLoginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.logout_option){
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.settings_option){
            SendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.find_people_option){
            SendUserToFindFriendsActivity();

        }
        if(item.getItemId() == R.id.create_group_chat){
            requestGroupCreate();
        }
        return true;
    }

    private void requestGroupCreate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(getApplicationContext());
        groupNameField.setHint("eg. Fantastic4");
        builder.setView(groupNameField);
        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Field cannot be empty ", Toast.LENGTH_SHORT).show();
                }else{
                    createNewgroup(groupName);
                    Log.d(MainActivity.ACTIVITY_SERVICE,groupName);
                }
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();


    }

    private void createNewgroup(String groupName) {
        rootref.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" Group is created successfull", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingintent = new Intent(MainActivity.this, SettingsActivity.class);
//        settingintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingintent);
//        finish();
    }
    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriends.class);
//        findFriendsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendsIntent);
//        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(user == null){
            SendUserToLoginActivity();
        }else{
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootref.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child("name").exists())){
//                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();
                }else{
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}