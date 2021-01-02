package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class videoViewer extends AppCompatActivity {
    private ImageButton backbutton,playbutton;
    private String reciverid,msgid,CurrentUserId;
    private boolean isplaying=false;
    private TextView starttime,totaltime;
    private DatabaseReference userRef,dataref;
    private FirebaseAuth mAuth;
    private VideoView videoview;
    private ProgressBar progressBar;
    private int current = 0, duration = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);

        reciverid = getIntent().getExtras().get("userIds").toString();
        msgid = getIntent().getExtras().get("pdfid").toString();

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        initialization();
        imagepreviewmethod(msgid);
        if(reciverid.equals(CurrentUserId)){
            reciverimagemessage(msgid);
        }else{
            imagepreviewmethod(msgid);
        }
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (isplaying){
                   videoview.pause();
                   progressmethod();
                   isplaying = false;
                   playbutton.setImageResource(R.mipmap.playbutton);
               }else{
                   videoview.start();
                   isplaying = true;
                   playbutton.setImageResource(R.mipmap.pause);
               }
            }
        });

        usernameMethod(reciverid);
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
    private void progressmethod() {
        do {
            current = videoview.getCurrentPosition()/1000;
            String currentString = String.format("%02d:%02d",current/60,current%60);
            starttime.setText(currentString);
            try {
                int currentpersent = current * 100/duration;
                progressBar.setProgress(currentpersent);

            }catch (Exception e){

            }

        }while (progressBar.getProgress() <= 100);
    }

    private void imagepreviewmethod(String msg) {
        File imagefile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Video/Send/"+msg+".mp4");
        String imagestr = imagefile.getAbsolutePath();
        videoview.setVideoPath(imagestr);
        videoview.requestFocus();
        isplaying = true;
        playbutton.setImageResource(R.mipmap.pause);
        timemethod();
        videoview.start();
        Toast.makeText(this,imagestr,Toast.LENGTH_SHORT).show();
    }


    private void reciverimagemessage(String msgid){
        File imagefile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Video/reciver/"+msgid+".mp4");
        String imagestr = imagefile.getAbsolutePath();
        videoview.setVideoPath(imagestr);
        videoview.requestFocus();
        isplaying = true;
        playbutton.setImageResource(R.mipmap.pause);
        timemethod();
        videoview.start();
        Toast.makeText(this,imagestr,Toast.LENGTH_SHORT).show();
    }

    private void timemethod() {
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                duration = mp.getDuration()/1000;
                String DurationString = String.format("%02d:%02d",duration/60,duration%60);
                totaltime.setText(DurationString);
            }
        });
    }



    private void initialization() {
        videoview = (VideoView) findViewById(R.id.videoView);
        backbutton = (ImageButton) findViewById(R.id.back);
        playbutton = (ImageButton) findViewById(R.id.playButton);
        progressBar = (ProgressBar) findViewById(R.id.processbar);
        starttime = (TextView) findViewById(R.id.startTime);
        totaltime = (TextView) findViewById(R.id.totalTime);
        progressBar.setMax(100);
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
