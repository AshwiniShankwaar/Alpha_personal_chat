package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class postcomment extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText commentbox;
    private CircleImageView userprofile;
    private ImageButton post;
    private RecyclerView recyclerView;
    private String postid,publisherid;
    private DatabaseReference ppostref;
    private FirebaseAuth mAuth;
    private String currentuserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcomment);

        intialization();

        Intent intent = getIntent();
        postid = intent.getStringExtra("postids");
        publisherid = intent.getStringExtra("publisherid");

        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        ppostref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        getprofileimage();
        postbuttonmethod();
    }

    public void getprofiledetails(final String userid, final CircleImageView userdp, final TextView profilename){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
              if (dataSnapshot.hasChild("image")){
                  String userprofiledp = dataSnapshot.child("image").getValue().toString();
                  Picasso.get().load(userprofiledp).into(userdp);
                  profilename.setText(username);

              }else{
                  Picasso.get().load(R.mipmap.defaultdp).into(userdp);
                  profilename.setText(username);
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<commentmodelclass>().setQuery(ppostref,commentmodelclass.class).build();
        FirebaseRecyclerAdapter<commentmodelclass,commentviewholder> adapter = new FirebaseRecyclerAdapter<commentmodelclass, commentviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final commentviewholder commentviewholder, int i, @NonNull final commentmodelclass commentmodelclass) {

                final String listcomment_id = getRef(i).getKey();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid).child(listcomment_id);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     final String publicerid = dataSnapshot.child("publisherId").getValue().toString();
                     final String commenttext = dataSnapshot.child("Comment").getValue().toString();
                     DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Comment_reply");
                     databaseReference.addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if(dataSnapshot.hasChild(listcomment_id)){
                              commentviewholder.commentsection.setVisibility(View.VISIBLE);
                              commentviewholder.replysection.setVisibility(View.VISIBLE);
                              commentviewholder.commenttext.setText(commenttext);
                              getprofiledetails(publicerid,commentviewholder.commentuserdp,commentviewholder.commentusername);
                              ArrayList replyidslist = new ArrayList<>();
                              for(DataSnapshot replyidsdatasnapshot: dataSnapshot.getChildren()){
                                  String replypushids = replyidsdatasnapshot.getChildren().toString();
                                  replyidslist.add(replypushids);
                              }

                              for (Object o : replyidslist) {
                                  if (dataSnapshot.hasChild(String.valueOf(o))){
                                      String userid = dataSnapshot.child(String.valueOf(o)).child("userid").getValue().toString();
                                      String replytext = dataSnapshot.child(String.valueOf(o)).child("replytext").getValue().toString();

                                      commentviewholder.replytext.setText(replytext);

                                      getprofiledetails(userid,commentviewholder.replyuserdp,commentviewholder.replyusername);
                                  }
                              }

                          }else {
                              commentviewholder.replysection.setVisibility(View.GONE);
                              commentviewholder.commentsection.setVisibility(View.VISIBLE);
                              commentviewholder.commenttext.setText(commenttext);
                              getprofiledetails(publicerid,commentviewholder.commentuserdp,commentviewholder.commentusername);
                              commentviewholder.commentsection.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      replypopupmethod(listcomment_id);
                                  }
                              });
                          }
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public commentviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.commentitem, parent, false);
                return new commentviewholder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void replypopupmethod(final String commentid){
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(),R.style.AlertDialogbox);
        builder.setTitle("Reply");
        final EditText groupnameField = new EditText(getApplicationContext());

        groupnameField.setHint("e.g: example");
        builder.setView(groupnameField);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupname = groupnameField.getText().toString();
                if (TextUtils.isEmpty(groupname)){
                    Toast.makeText(getApplicationContext(), "You can't post emputy reply", Toast.LENGTH_SHORT).show();
                }else {
                    uploadreply(commentid,groupname);
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
    private void uploadreply(String commentid,String replytext){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Comment_reply").child(commentid);
        Map replymap = new HashMap();
        replymap.put("userid",currentuserId);
        replymap.put("replytext",replytext);

        databaseReference.push().setValue(replymap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Reply posted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Error occurs, please report the error",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void getprofileimage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.hasChild("image")){
                   Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(userprofile);
               }else{
                   Picasso.get().load(R.mipmap.defaultdp).into(userprofile);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postbuttonmethod() {
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(commentbox.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"You can't post an empty comment",Toast.LENGTH_SHORT).show();
                }else{
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Post_notification").child(postid).child("Comment");
                    Map postmap = new HashMap();
                    postmap.put("Comment",commentbox.getText().toString());
                    postmap.put("publisherId",publisherid);

                    reference.push().setValue(postmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Map notification = new HashMap();
                            notification.put("Comment",commentbox.getText().toString());
                            databaseReference.child(publisherid).push().updateChildren(notification).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    commentbox.setText("");
                                }
                            });


                        }
                    });

                }
            }
        });
    }


    private void intialization() {
        toolbar = findViewById(R.id.commenttoolbar);
        commentbox = findViewById(R.id.edittext);
        userprofile = findViewById(R.id.currentuserdp);
        post = findViewById(R.id.post);
        recyclerView = findViewById(R.id.commentrecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("Comment");
    }
    public static class commentviewholder extends RecyclerView.ViewHolder{
        private LinearLayout commentsection;
        private LinearLayout replysection;
        private CircleImageView commentuserdp,replyuserdp;
        private TextView commentusername,replyusername,commenttext,replytext;
        public commentviewholder(@NonNull View itemView) {
            super(itemView);
            commentsection = itemView.findViewById(R.id.commentsection);
            replysection = itemView.findViewById(R.id.replysection);
            replyuserdp = itemView.findViewById(R.id.replyuserdp);
            commentuserdp = itemView.findViewById(R.id.userdp);
            commentusername = itemView.findViewById(R.id.username);
            commenttext = itemView.findViewById(R.id.usercommenttext);
            replytext = itemView.findViewById(R.id.usercommentreplytext);
            replyusername = itemView.findViewById(R.id.replyusername);
        }
    }
}

