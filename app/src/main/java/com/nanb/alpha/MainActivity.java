package com.nanb.alpha;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Toolbar main_toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private tabAddapter Tab;

     private FirebaseUser currentUser;
     private FirebaseAuth mAuth;
     private DatabaseReference rootref;
     private String currentuserExit =  "currentUserExist";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFolderforthisapp();


        main_toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setTitle("Alpha");

        viewPager = (ViewPager) findViewById(R.id.main_tab_pager);
        Tab = new tabAddapter(getSupportFragmentManager());
        viewPager.setAdapter(Tab);

        tabLayout = (TabLayout) findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(viewPager);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootref = FirebaseDatabase.getInstance().getReference();

    }

    private void createFolderforthisapp() {
        File file = new File(Environment.getExternalStorageDirectory()+"/Alpha");
        Boolean success = true;
        if(!file.exists()){
            file.mkdir();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            sendusertologinactivity();
        }else{
            updateuserStatus("online");
            if(currentuserExit.equals("currentUserExist")) {
                verifyuserexiting();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null){
            updateuserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null){
            updateuserStatus("offline");
        }
    }

    private void verifyuserexiting() {
        String currentuserid = mAuth.getCurrentUser().getUid();
        rootref.child("User").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    currentuserExit = "UserExit";
                }else{
                    sendusertosetting();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendusertologinactivity() {
        Intent loginIntent = new Intent(MainActivity.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.Log_out){
            mAuth.signOut();
            sendusertologinactivity();
        }
        if (item.getItemId() == R.id.setting){
         sendusertosettingactivity();
        }
        if (item.getItemId() == R.id.Findfriend){
           findFrienduser();
        }
        if (item.getItemId() == R.id.Group){
           requestNewGroup();
        }
        if (item.getItemId() == R.id.Friendrequest){
            friendrequest();
        }

        return true;
    }

    private void sendusertosettingactivity() {
        Intent Intent = new Intent(MainActivity.this,Setting.class);
        startActivity(Intent);
    }

    private void friendrequest() {
      Intent frequestIntent = new Intent(MainActivity.this,friendRequest.class);
      startActivity(frequestIntent);
    }

    private void findFrienduser() {
        Intent ffIntent = new Intent(MainActivity.this,findFriend.class);
        startActivity(ffIntent);
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogbox);
        LinearLayout linearLayout = new LinearLayout(this);
        builder.setTitle("Enter Group name");
        final EditText groupnameField = new EditText(MainActivity.this);
        groupnameField.setHint("e.g: example");
        linearLayout.addView(groupnameField);
        linearLayout.setPadding(5, 5,  5, 5 );
        builder.setView(linearLayout);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              String groupname = groupnameField.getText().toString();
              if (TextUtils.isEmpty(groupname)){
                  Toast.makeText(MainActivity.this, "Write group name", Toast.LENGTH_SHORT).show();
              }else {
                  getNewGroup(groupname);
              }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.cancel();

            }
        });
        builder.show();

    }

    private void getNewGroup(final String groupname) {
        groupmethos(groupname);
    }

    private void groupmethos(final String groupname) {
        DatabaseReference groupref = rootref.child("Group").push();
        final String grouppushid = groupref.getKey();
        Map groupmap = new HashMap<>();
        groupmap.put("GroupprofileName",groupname);
        groupmap.put("Creater",currentUser.getUid());
        groupmap.put("StatusGroup","");
        //groupmap.put("profileimage","");
        groupmap.put("id",grouppushid);
        rootref.child("Group").child(grouppushid).updateChildren(groupmap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                rootref.child("GroupMessage").child(grouppushid).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Map addtogroup = new HashMap<>();
                            addtogroup.put("Groupid",grouppushid);
                            rootref.child("userconnectedtogroup").child(currentUser.getUid()).updateChildren(addtogroup).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Toast.makeText(MainActivity.this,groupname + "group Created successfully",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });

            }
        });
    }

    private void sendusertosetting() {
        Intent settingIntent = new Intent(MainActivity.this,profile.class);
        startActivity(settingIntent);
    }
    private void updateuserStatus(String state){
        String savecurrentDate,savecurrentTime;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());
        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("Time",savecurrentTime);
        onlineState.put("Date",savecurrentDate);
        onlineState.put("State",state);
        String currentUser = mAuth.getCurrentUser().getUid();
        rootref.child("User").child(currentUser).child("userState").updateChildren(onlineState);
    }
}
