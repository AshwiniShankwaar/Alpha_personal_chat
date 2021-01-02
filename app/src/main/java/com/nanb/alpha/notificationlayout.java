package com.nanb.alpha;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class notificationlayout extends Fragment {

    private View notificationview;
    private RecyclerView notificationrecycler;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref,userRef,notificationref;
    private String currentUserid;
    public notificationlayout() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        notificationview =  inflater.inflate(R.layout.fragment_notificationlayout, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        notificationref = FirebaseDatabase.getInstance().getReference().child("Post_notification");
        initialization();
        return notificationview;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
    }


    @Override
    public void onDestroy() {
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
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
        FirebaseRecyclerOptions<postmodelclass> options = new FirebaseRecyclerOptions.Builder<postmodelclass>().setQuery(notificationref,postmodelclass.class).build();
        FirebaseRecyclerAdapter<postmodelclass,notification_viewholder> adapter = new FirebaseRecyclerAdapter<postmodelclass, notification_viewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull notification_viewholder notification_viewholder, int i, @NonNull postmodelclass postmodelclass) {
                final String postIds = getRef(i).getKey();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Post").child(postIds);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     String publicerid = dataSnapshot.child("PostAdmin").getValue().toString();
                     String posttype = dataSnapshot.child("type").getValue().toString();
                     String posturl = dataSnapshot.child("url").getValue().toString();
                     if(publicerid == currentUserid){
                         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Post_notification").child(postIds);
                         databaseReference.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if(dataSnapshot.hasChild("Like")){
                                   String likeduserid = dataSnapshot.child("Like").getChildren().toString();
                                   getuserdetail(likeduserid,notification_viewholder,"Like");
                                   if(posttype.equals("image")){
                                       notification_viewholder.postimage.setVisibility(View.VISIBLE);
                                       Picasso.get().load(posturl).into(notification_viewholder.postimage);
                                   }else{
                                       notification_viewholder.postimage.setVisibility(View.GONE);
                                   }
                               }else{
                                   String likeduserid = dataSnapshot.child("Comment").getChildren().toString();
                                   getuserdetail(likeduserid,notification_viewholder,"Comment");
                               }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });
                     }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public notification_viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.postnotification,parent,false);
                return new notification_viewholder(view);
            }
        };
        notificationrecycler.setAdapter(adapter);
        adapter.startListening();

    }

    private void getuserdetail(String likeduserid, notification_viewholder notification_viewholder ,String Posttype) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(likeduserid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.hasChild("image")){
                 String profileimage = dataSnapshot.child("image").getValue().toString();
                 String profileusername = dataSnapshot.child("Username").getValue().toString();

                 Picasso.get().load(profileimage).into(notification_viewholder.profiledp);
                 if(Posttype.equals("Like")){
                     notification_viewholder.postmesssage.setText(profileusername+" has Liked your post");
                 }else{
                     notification_viewholder.postmesssage.setText(profileusername+" comment on your post");
                 }

             }else{
                 String profileusername = dataSnapshot.child("Username").getValue().toString();

                 Picasso.get().load(R.mipmap.defaultdp).into(notification_viewholder.profiledp);
                 if(Posttype.equals("Like")){
                     notification_viewholder.postmesssage.setText(profileusername+" has Liked your post");
                 }else{
                     notification_viewholder.postmesssage.setText(profileusername+" comment on your post");
                 }
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class notification_viewholder extends RecyclerView.ViewHolder{

        ImageView profiledp,postimage;
        TextView postmesssage;
        public notification_viewholder(@NonNull View itemView) {
            super(itemView);
            profiledp = itemView.findViewById(R.id.profiledp);
            postimage = itemView.findViewById(R.id.postimage);
            postmesssage = itemView.findViewById(R.id.textcomment);
        }
    }

    private void initialization() {
        notificationrecycler = notificationview.findViewById(R.id.notificationlist);
        notificationrecycler.setLayoutManager(new LinearLayoutManager(getContext()));

    }

}
