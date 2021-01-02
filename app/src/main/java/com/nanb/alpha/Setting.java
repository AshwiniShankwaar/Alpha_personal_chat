package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setting extends AppCompatActivity {
    private static final int REQUEST_INVITE = 438 ;
    private CircleImageView img;
    private TextView username, status;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private String currentUserid;
    private Toolbar stoolbar;
    private Button edit,view;
    private LinearLayout Account,Chats,help,invite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initialization();
        sendusertoprofile();


        mauth = FirebaseAuth.getInstance();
        rootref = FirebaseDatabase.getInstance().getReference();
        currentUserid = mauth.getCurrentUser().getUid();

        fatchdata();
        Accountmethod();
        chatmethod();
        helpmethod();
        invitefriend();

    }

    private void invitefriend() {
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked();
            }
        });
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Invite Friends")
                .setMessage("Hi there,just found awesome social media Application Just download it and follow me ")
                .setDeepLink(Uri.parse("https://google.com"))
                .setCustomImage(Uri.parse("https://google.com"))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("invite", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("invite", "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Toast.makeText(getApplicationContext(),"Failed to send invite",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void helpmethod() {
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Help.class);
                startActivity(intent);
            }
        });
    }

    private void chatmethod() {
        Chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),chatssetting.class);
                startActivity(intent);
            }
        });
    }

    private void Accountmethod() {
        Account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Accountsetting.class);
                startActivity(intent);
            }
        });
    }

    private void fatchdata() {
        rootref.child("User").child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Username"))&&(dataSnapshot.hasChild("image"))) {
                    String profileUsername = dataSnapshot.child("Username").getValue().toString();
                    String profilestatus = dataSnapshot.child("status").getValue().toString();
                    String profileImage = dataSnapshot.child("image").getValue().toString();
                    username.setText(profileUsername);
                    status.setText(profilestatus);
                    Picasso.get().load(profileImage).into(img);
                }else{
                    String profileUsername = dataSnapshot.child("Username").getValue().toString();
                    String profilestatus = dataSnapshot.child("status").getValue().toString();
                    username.setText(profileUsername);
                    status.setText(profilestatus);
                    Picasso.get().load(R.mipmap.defaultdp).into(img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendusertoprofile() {
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(Setting.this, profile.class);
                startActivity(profileIntent);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Setting.this, userprofile.class);
                startActivity(profileIntent);
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Setting.this, userprofile.class);
                startActivity(profileIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
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
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("User").child(currentUser).child("userState").updateChildren(onlineState);
    }

    private void initialization() {
        img = (CircleImageView) findViewById(R.id.profile_image);
        username = (TextView) findViewById(R.id.Username);
        status = (TextView) findViewById(R.id.status);
        stoolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(stoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Setting");

        Account = findViewById(R.id.Account);
        Chats = findViewById(R.id.Chats);
        help = findViewById(R.id.Help);
        invite = findViewById(R.id.invite);
        edit = findViewById(R.id.edit);
        view = findViewById(R.id.view);
    }
}
