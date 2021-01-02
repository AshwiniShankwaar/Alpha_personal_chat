package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

public class userprofile extends AppCompatActivity {

    private CircleImageView profileDp;
    private TextView fullname, ustatus, uUsername,bio,phoneno;
    private DatabaseReference userRef;
    private FirebaseAuth mauth;
    private Toolbar mtoolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        mauth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("User").child(mauth.getCurrentUser().getUid());

        intialization();
        requestinfo();
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
    private void requestinfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String userfullname = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
                     String useremail = dataSnapshot.child("Email").getValue().toString();
                    String userphone = dataSnapshot.child("Phone_no").getValue().toString();

                    Picasso.get().load(userImage).into(profileDp);
                    fullname.setText(userfullname);
                    ustatus.setText(userstatus);
                    uUsername.setText(username);
                    bio.setText(useremail);
                    phoneno.setText(userphone);

                }else{
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String userfullname = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
                    String useremail = dataSnapshot.child("Email").getValue().toString();
                    String userphone = dataSnapshot.child("Phone_no").getValue().toString();

                    Picasso.get().load(R.mipmap.defaultdp).into(profileDp);
                    fullname.setText(userfullname);
                    ustatus.setText(userstatus);
                    uUsername.setText(username);
                    bio.setText(useremail);
                    phoneno.setText(userphone);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void intialization() {
        profileDp = (CircleImageView) findViewById(R.id.profileImage);
        fullname = (TextView) findViewById(R.id.name);
        ustatus = (TextView) findViewById(R.id.status);
        uUsername = (TextView) findViewById(R.id.username);
        bio = findViewById(R.id.Bio);
        phoneno = findViewById(R.id.phone);
        mtoolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }
}
