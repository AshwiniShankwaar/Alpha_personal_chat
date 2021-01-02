package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class imagereview extends AppCompatActivity {

    private String reciveruserid;
    private String msg,msgid,msgtype;
    private DatabaseReference userRef,dataref;
    private ImageButton backbutton;
    private ImageView imagepreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagereview);
        reciveruserid = getIntent().getExtras().get("userIds").toString();
        msg = getIntent().getExtras().get("imagestring").toString();
        msgid = getIntent().getExtras().get("imageid").toString();
        msgtype = getIntent().getExtras().get("imagetype").toString();
        initialization();
        imagepreviewmethod(msg);
        if(msgtype.equals("sender")){
            reciverimagemessage(msgid);
        }else{
            imagepreviewmethod(msg);
        }
        usernameMethod(reciveruserid);

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
    private void imagepreviewmethod(String msg) {
        File imagefile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Photos/"+msg);
        String imagestr = imagefile.getAbsolutePath();
        Picasso.get().load(new File(imagestr)).into(imagepreview);
        Toast.makeText(this,imagestr,Toast.LENGTH_SHORT).show();
    }

    private void reciverimagemessage(String msgid){
        File imagefile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Photos/recived/"+msgid+".jpg");
        String imagestr = imagefile.getAbsolutePath();
        Picasso.get().load(new File(imagestr)).into(imagepreview);
        Toast.makeText(this,imagestr,Toast.LENGTH_SHORT).show();
    }

    private void initialization() {
        imagepreview = (ImageView) findViewById(R.id.imageView);
        backbutton = (ImageButton) findViewById(R.id.back);
    }

    private void usernameMethod(final String reciveruserid){
        final String[] usernm = {null};
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        dataref = userRef.child(reciveruserid);
        dataref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           usernm[0] = dataSnapshot.child("Username").getValue().toString();
           backbuttonmethod(reciveruserid,usernm[0]);
           Toast.makeText(getApplicationContext(), usernm[0] +" "+reciveruserid,Toast.LENGTH_SHORT).show();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void backbuttonmethod(final String reciveruserid, final String s) {
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personalChatIntent = new Intent(getApplicationContext(),personalChat.class);
                personalChatIntent.putExtra("userIds",reciveruserid);
                personalChatIntent.putExtra("fullname",s);
                startActivity(personalChatIntent);
            }
        });
    }
}
