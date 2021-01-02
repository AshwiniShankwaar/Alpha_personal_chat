package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class findFriend extends AppCompatActivity {
    private Toolbar ftoolbar;
    private RecyclerView recyclerlist;
    private DatabaseReference userref;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private String currentUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        userref = FirebaseDatabase.getInstance().getReference().child("User");
        mauth = FirebaseAuth.getInstance();
        rootref = FirebaseDatabase.getInstance().getReference();
        currentUserid = mauth.getCurrentUser().getUid();
        findfriend_incluize();
    }

    private void findfriend_incluize() {
        recyclerlist = (RecyclerView) findViewById(R.id.Findfriend_recyclerList);
        recyclerlist.setLayoutManager(new LinearLayoutManager(this));
        ftoolbar = (Toolbar) findViewById(R.id.Findfriend_toolbar);
        setSupportActionBar(ftoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
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

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }

        FirebaseRecyclerOptions<contact> options = new FirebaseRecyclerOptions.Builder<contact>()
                .setQuery(userref,contact.class)
                .build();
        FirebaseRecyclerAdapter<contact,Findfriendviewholder> adapter = new FirebaseRecyclerAdapter<contact, Findfriendviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Findfriendviewholder findfriendviewholder, int i, @NonNull contact contact) {
                final String userid = getRef(i).getKey();
                findfriendviewholder.userName.setText(contact.getName());
                findfriendviewholder.userstatus.setText(contact.getStatus());
                Picasso.get().load(contact.getImage()).into(findfriendviewholder.profilepic);
                findfriendviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileViewIntent = new Intent(findFriend.this,profileView.class);
                        profileViewIntent.putExtra("userId", userid);
                        startActivity(profileViewIntent);
                    }
                });
            }

            @NonNull
            @Override
            public Findfriendviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplaylayout, parent, false);
                Findfriendviewholder viewHolder = new Findfriendviewholder(view);
                return viewHolder;
            }
        };

        recyclerlist.setAdapter(adapter);
        adapter.startListening();

    }


    public static class Findfriendviewholder extends RecyclerView.ViewHolder{
        TextView userName, userstatus ;
        CircleImageView profilepic;
        String currentState;
        public Findfriendviewholder (@NonNull View itemView){
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.status);
            profilepic = itemView.findViewById(R.id.userprofileImage);
            currentState = "new";
        }
    }
}
