package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class group extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference conref,groupref;
    private FirebaseAuth mAuth;
    private String Currentuserid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        intialiation();
        mAuth = FirebaseAuth.getInstance();
        Currentuserid = mAuth.getCurrentUser().getUid();
        conref = FirebaseDatabase.getInstance().getReference().child("userconnectedtogroup").child(Currentuserid);
        groupref = FirebaseDatabase.getInstance().getReference().child("Group");
    }

    private void intialiation() {
        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        FirebaseRecyclerOptions option = new FirebaseRecyclerOptions.Builder<modelclass>().setQuery(groupref,modelclass.class).build();

        FirebaseRecyclerAdapter<modelclass,group_viewHolder> adapter = new FirebaseRecyclerAdapter<modelclass, group_viewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull final group_viewHolder group_viewHolder, int i, @NonNull modelclass modelclass) {
                String userid = getRef(i).getKey();
                groupref.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if(dataSnapshot.hasChild("profileimage")){
                          String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                          String groupname = dataSnapshot.child("GroupprofileName").getValue().toString();
                          String groupStatus = dataSnapshot.child("StatusGroup").getValue().toString();

                          group_viewHolder.groupname.setText(groupname);
                          group_viewHolder.groupStatus.setText(groupStatus);
                          Picasso.get().load(profileimage).into(group_viewHolder.profileimage);
                      }else{
                          String groupname = dataSnapshot.child("GroupprofileName").getValue().toString();
                          String groupStatus = dataSnapshot.child("StatusGroup").getValue().toString();

                          group_viewHolder.groupname.setText(groupname);
                          group_viewHolder.groupStatus.setText(groupStatus);
                          Picasso.get().load(R.mipmap.groupicon).into(group_viewHolder.profileimage);
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public group_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grouplayout,parent,false);
               group_viewHolder groupholder = new group_viewHolder(view);
                return groupholder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class group_viewHolder extends RecyclerView.ViewHolder{
        TextView groupname,groupStatus;
        CircleImageView profileimage;
        public group_viewHolder(@NonNull View itemView) {
            super(itemView);
            groupname = itemView.findViewById(R.id.Group_name);
            groupStatus = itemView.findViewById(R.id.Group_Status);
            profileimage = itemView.findViewById(R.id.group_profile_dp);
        }
    }
}
