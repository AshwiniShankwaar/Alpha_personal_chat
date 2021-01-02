package com.nanb.alpha;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class home extends Fragment {

    private View home;
    private FirebaseAuth mAuth;
    private String currentuserid,savecurrentTime,savecurrentDate;
    private DatabaseReference rootref,usersref,postref;
    private RecyclerView recycle;

    public home() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        home =  inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        usersref = FirebaseDatabase.getInstance().getReference().child("User");
        postref = FirebaseDatabase.getInstance().getReference().child("Post");
        initialization();
        return home;
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
        FirebaseRecyclerOptions<postmodelclass> options = new FirebaseRecyclerOptions.Builder<postmodelclass>().setQuery(postref,postmodelclass.class).build();
        FirebaseRecyclerAdapter<postmodelclass,postviewholser> adapter = new FirebaseRecyclerAdapter<postmodelclass, postviewholser>(options) {
            @Override
            protected void onBindViewHolder(@NonNull postviewholser postviewholser, int i, @NonNull postmodelclass postmodelclass) {
                final String postid = getRef(i).getKey();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Post").child(postid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     String Userid = dataSnapshot.child("PostAdmin").getValue().toString();
                     String type = dataSnapshot.child("type").getValue().toString();
                     DatabaseReference contactref = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentuserid);
                     contactref.addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if(dataSnapshot.hasChild(Userid)){
                              profilemethod(Userid,postviewholser);
                              if(type == "image"){
                                  postviewholser.postvideo.setVisibility(View.GONE);
                                  postviewholser.playbutton.setVisibility(View.GONE);
                                  postviewholser.postimage.setVisibility(View.VISIBLE);
                                  Picasso.get().load(dataSnapshot.child("url").getValue().toString()).into(postviewholser.postimage);
                                  contextmethod(postid,postviewholser);
                                  likedby(postid,postviewholser);
                                  nlikecount(postid,postviewholser);
                                  islike(postid,postviewholser);
                                  postviewholser.comment.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = new Intent(home.getContext(),postcomment.class);
                                          startActivity(intent);
                                      }
                                  });


                                  viewcommentmethod(postid,postviewholser);

                              }else{
                                  postviewholser.postvideo.setVisibility(View.VISIBLE);

                                  postviewholser.postimage.setVisibility(View.GONE);
                                  contextmethod(postid,postviewholser);
                                  likedby(postid,postviewholser);
                                  nlikecount(postid,postviewholser);
                                  islike(postid,postviewholser);
                                  viewcommentmethod(postid,postviewholser);
                                  postviewholser.comment.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = new Intent(home.getContext(),postcomment.class);
                                          startActivity(intent);
                                      }
                                  });
                                  postviewholser.postvideo.setVideoURI(Uri.parse(dataSnapshot.child("url").getValue().toString()));
                                  postviewholser.postvideo.requestFocus();
                                  final boolean[] isclicked = {false};

                                  postviewholser.postvideo.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          if(isclicked[0] == false){
                                              postviewholser.playbutton.setVisibility(View.VISIBLE);
                                              postviewholser.playbutton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      postviewholser.postvideo.start();
                                                      isclicked[0] = true;
                                                  }
                                              });
                                          }else {
                                              postviewholser.playbutton.setVisibility(View.VISIBLE);
                                              postviewholser.playbutton.setImageResource(R.mipmap.pausebutton);
                                              postviewholser.playbutton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      postviewholser.postvideo.stopPlayback();
                                                      isclicked[0] = false;
                                                  }
                                              });
                                          }
                                      }
                                  });
                              }
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
            public postviewholser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.postlayout,parent,false);
                return new postviewholser(view);
            }
        };
        recycle.setAdapter(adapter);
        adapter.startListening();
    }

    private void viewcommentmethod(final String postIds, final postviewholser postviewholser) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.hasChild(postIds)){
                 postviewholser.commentsection.setVisibility(View.VISIBLE);
                 //postviewholser.replysection.setVisibility(View.VISIBLE);
                 postviewholser.viewallcomments.setVisibility(View.VISIBLE);
                 postviewholser.viewallcomments.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Intent intent = new Intent(home.getContext(),postcomment.class);
                         startActivity(intent);
                     }
                 });
                 long commentcount = dataSnapshot.child(postIds).getChildrenCount();
                 postviewholser.viewallcomments.setText("View all "+commentcount+" Comments");
                 reference.child(postIds).orderByKey().limitToFirst(1).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       final String commentid = dataSnapshot.getChildren().toString();
                       final String userid = dataSnapshot.child(commentid).child("publisherId").getValue().toString();
                       final String usercomment = dataSnapshot.child(commentid).child("Comment").getValue().toString();
                       DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Comment_reply");
                       databaseReference.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(commentid)){
                                //add cammand to show the reply of the comment
                                Toast.makeText(home.getContext(),"Please see to the code once",Toast.LENGTH_SHORT).show();
                            }else{
                                postviewholser.replysection.setVisibility(View.GONE);
                                postviewholser.commentsection.setVisibility(View.VISIBLE);
                                postviewholser.commentuserid.setText(userid);
                                postviewholser.commenttext.setText(usercomment);


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


             }else{
                 postviewholser.commentsection.setVisibility(View.GONE);
                 postviewholser.replysection.setVisibility(View.GONE);
                 postviewholser.viewallcomments.setVisibility(View.GONE);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void contextmethod(String postIds, final postviewholser postviewholser){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Post").child(postIds);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.hasChild("caption")){
                 String captiontext = dataSnapshot.child("caption").getValue().toString();
                 if(captiontext.isEmpty()) {
                     postviewholser.descriptionl.setVisibility(View.GONE);
                 }else{
                     postviewholser.descriptionl.setText(captiontext);
                 }
             }else{
                 postviewholser.descriptionl.setVisibility(View.GONE);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void islike(String postIds, final postviewholser postviewholser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postIds);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Post_notification").child(postIds).child("Like");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.hasChild(currentuserid)){
                 postviewholser.like.setImageResource(R.drawable.like);
                 postviewholser.like.setTag("Liked");
                 postviewholser.like.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         reference.child(currentuserid).removeValue();
                         databaseReference.child(currentuserid).removeValue();

                     }
                 });
             }else{
                 postviewholser.like.setImageResource(R.drawable.heart);
                 postviewholser.like.setTag("notliked");
                 postviewholser.like.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         reference.child(currentuserid).push();
                         databaseReference.child(currentuserid).push();



                     }
                 });
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void likedby(final String postIds, final postviewholser postviewholser){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.hasChild(postIds)){
                 postviewholser.likedby.setVisibility(View.VISIBLE);
                 final long countlike = dataSnapshot.child(postIds).getChildrenCount();
                 if(countlike > 1 ){
                     reference.orderByKey().limitToFirst(1).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             //loiked by
                             String lastlikedid = dataSnapshot.getChildren().toString();
                             usersref.child(lastlikedid).addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                     long otherlike = countlike - 1;
                                     postviewholser.likedby.setText("Liked by "+dataSnapshot.hasChild("naame")+" and "+otherlike+" others." );
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });

                             Toast.makeText(home.getContext(),"check the liked by method",Toast.LENGTH_SHORT).show();
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });
                 }else{
                     reference.orderByKey().limitToFirst(1).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             //loiked by
                             String lastlikedid = dataSnapshot.getChildren().toString();
                             usersref.child(lastlikedid).addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                     postviewholser.likedby.setText("Liked by"+dataSnapshot.hasChild("naame"));
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });

                             Toast.makeText(home.getContext(),"check the liked by method",Toast.LENGTH_SHORT).show();
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });
                 }

             }else{
                 postviewholser.likedby.setVisibility(View.GONE);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void nlikecount(String postIds,final postviewholser postviewholser){
        rootref.child("Likes").child(postIds).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             postviewholser.likecount.setText(dataSnapshot.getChildrenCount()+" Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void profilemethod(String postadmin, final postviewholser postviewholser) {
        usersref.child(postadmin).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.hasChild("image")){
                 String postadmindp = dataSnapshot.child("image").getValue().toString();
                 String postadminusername = dataSnapshot.child("name").getValue().toString();

                 postviewholser.adminname.setText(postadminusername);
                 Picasso.get().load(postadmindp).into(postviewholser.admindp);

             }else{

                 String postadminusername = dataSnapshot.child("name").getValue().toString();

                 postviewholser.adminname.setText(postadminusername);
                 Picasso.get().load(R.mipmap.defaultdp).into(postviewholser.admindp);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initialization() {
        recycle = home.findViewById(R.id.postrecycle);
        recycle.setLayoutManager(new LinearLayoutManager(getContext()));
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());
    }
}

class postviewholser extends RecyclerView.ViewHolder{
    CircleImageView admindp;
    ImageView more,postimage,like,comment,save;
    VideoView postvideo;
    TextView likedby,descriptionl,adminname,likecount,viewallcomments,commentuserid,commenttext,replyuserid,replytext;
    ImageButton playbutton;
    LinearLayout commentsection,replysection;
    public postviewholser(@NonNull View itemView) {
        super(itemView);
        admindp = itemView.findViewById(R.id.profilepic);
        more = itemView.findViewById(R.id.morebutton);
        postimage = itemView.findViewById(R.id.image);
        postvideo = itemView.findViewById(R.id.video);
        like = itemView.findViewById(R.id.likebutton);
        comment = itemView.findViewById(R.id.commentbutton);
        save = itemView.findViewById(R.id.savebutton);
        likedby = itemView.findViewById(R.id.likedby);
        descriptionl = itemView.findViewById(R.id.description);
        adminname = itemView.findViewById(R.id.adminusername);
        playbutton = itemView.findViewById(R.id.playButton);
        likecount = itemView.findViewById(R.id.likecount);
        viewallcomments = itemView.findViewById(R.id.viewallcomment);
        commentsection = itemView.findViewById(R.id.comment);
        commentuserid = itemView.findViewById(R.id.commentuserid);
        commenttext = itemView.findViewById(R.id.usercomment);
        replysection = itemView.findViewById(R.id.reply);
        replyuserid = itemView.findViewById(R.id.replyuserid);
        replytext = itemView.findViewById(R.id.userreply);
    }
}