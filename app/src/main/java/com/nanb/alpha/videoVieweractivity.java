package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class videoVieweractivity extends AppCompatActivity {
    private TextView send,retry;
    private VideoView videoview;
    private ImageButton play;
    private String intentimagepath,reciveruserid,CurrentUserId,savecurrenttime,savecurrentdate,myurl="",fullName,checker;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageTask uploadTask;
    private Uri imagefile,videouri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_vieweractivity);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        intentimagepath = getIntent().getExtras().get("videopath").toString();
        reciveruserid = getIntent().getExtras().get("userids").toString();
        fullName = getIntent().getExtras().get("fullname").toString();

        Toast.makeText(videoVieweractivity.this,intentimagepath,Toast.LENGTH_SHORT).show();
        imagefile = Uri.parse(intentimagepath);
        videodisplay();

        retrymethod();
        implimantation();
        sendmessage();
    }

    private void sendmessage() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videofilemessage(imagefile,intentimagepath);
            }
        });
    }

    public void videotemp(String videoPath, String msgpushid) throws FileNotFoundException {
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        String videooutputfilepath = null;
        if (bMap != null) {
            File videofile = new File(Environment.getExternalStorageDirectory() + "/Alpha/Video/temp");
            videooutputfilepath = videofile.getAbsolutePath() + "/" + msgpushid + ".png";
            FileOutputStream out = null;
            out = new FileOutputStream(videooutputfilepath);
            bMap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        return;
    }
    private void  videofilemessage(final Uri imagefile, final String vfilePath){
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Video");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + reciveruserid;
        final String msgReciverref = "Message/" + reciveruserid + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(reciveruserid).push();
        final String msgPushId = usermsgKeyRef.getKey();
        try {
            videotemp(vfilePath,msgPushId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final StorageReference filePath = imagereference.child(msgPushId+"."+"mp4");
        uploadTask = filePath.putFile(imagefile);
        String destinationfile = Environment.getExternalStorageDirectory()+"/Alpha/Video/Send/"+msgPushId+".mp4";
        copyFileOrDirectory(vfilePath,destinationfile);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myurl = downloadUri.toString();

                    uploadvideoTempfile(myurl,msgPushId,msgSenderref,msgReciverref);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(videoVieweractivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

            }
        });
    }

    private void uploadvideoTempfile(final String myUrl, final String msgPushId, final String msgSenderref, final String msgReciverref) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Video").child("temp");
        Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/Alpha/Video/temp/"+msgPushId+".png"));
        final StorageReference fileuploadpath = imagereference.child(msgPushId+".png");
        uploadTask = fileuploadpath.putFile(file);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return fileuploadpath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String  myfileUrl = downloadUri.toString();
                    //Toast.makeText(personalChat.this,myfileUrl,Toast.LENGTH_SHORT).show();
                    uploaddatatotheserver(myUrl,msgPushId,msgSenderref,msgReciverref,myfileUrl);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(videoVieweractivity.this,"Error:"+ e,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploaddatatotheserver(String myUrl, final String msgPushId, final String msgSenderref, final String msgReciverref,final String myfileurl) {


        Map msgpicbody = new HashMap();
        msgpicbody.put("message",myUrl);
        msgpicbody.put("name",imagefile.getLastPathSegment());
        msgpicbody.put("type","video");
        msgpicbody.put("ffrom",CurrentUserId);
        msgpicbody.put("to",reciveruserid);
        msgpicbody.put("date", savecurrentdate);
        msgpicbody.put("time",savecurrenttime);
        msgpicbody.put("messageId",msgPushId);
        msgpicbody.put("tempfile",myfileurl);

        Map msgBodyDetail = new HashMap();
        msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgpicbody);
        msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgpicbody);

        rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                  close_the_Activity();
                }else{
                    close_the_Activity();
                    Toast.makeText(videoVieweractivity.this,"error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void close_the_Activity() {
        Intent personalChatIntent = new Intent(videoVieweractivity.this,personalChat.class);
        personalChatIntent.putExtra("userIds",reciveruserid);
        personalChatIntent.putExtra("fullname",fullName);
        startActivity(personalChatIntent);
    }
    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
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
    private void retrymethod() {
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               retry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            File deleteoriginalfile = new File(intentimagepath);
                            if(deleteoriginalfile.exists()){
                                Boolean delete = deleteoriginalfile.delete();
                                if(delete){
                                    camera();
                                }else{
                                    camera();
                                    Toast.makeText(videoVieweractivity.this,"Error: video is not been deleted",Toast.LENGTH_SHORT).show();
                                }
                            }


                    }
                });
               //Toast.makeText(getApplicationContext(),"check this button",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void camera(){
        boolean data = checkCameraHardware(videoVieweractivity.this);
        if (data == true){
            checker = "imageCapture";
            //Toast.makeText(personalChat.this,"Camera exits",Toast.LENGTH_SHORT).show();
            cameraopen(checker);
        }else{
            Toast.makeText(videoVieweractivity.this,"Camera doesn't exits",Toast.LENGTH_SHORT).show();
        }
    }
    private void cameraopen(String checker) {
        Camera c = null;
        if(c == null) {
            try {
                Intent cameraIntent = new Intent(this,cameraintent.class);
                cameraIntent.putExtra("Checker",checker);
                cameraIntent.putExtra("userIds",reciveruserid);
                cameraIntent.putExtra("fullname",fullName);
                startActivity(cameraIntent);
                // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                c.release();
                c = null;
                Toast.makeText(videoVieweractivity.this, "Error:" + e, Toast.LENGTH_SHORT).show();
            }
        }else{
            c.release();
            c = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    private void videodisplay() {

       MediaController mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoview);
        videoview.setMediaController(mediacontroller);
        videouri = Uri.parse(intentimagepath);
        videoview.setVideoURI(videouri);
        videoview.requestFocus();
        videoview.start();
    }

    private void implimantation() {
        send = (TextView) findViewById(R.id.send);
        retry = (TextView) findViewById(R.id.retry);
        videoview = (VideoView) findViewById(R.id.videoviewer);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrenttime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentdate = currentDate.format(calendar.getTime());

    }
}
