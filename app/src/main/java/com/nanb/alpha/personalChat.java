package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class personalChat extends AppCompatActivity {
    private String msgReciverId, msgReciverName,ProfileImage,CurrentUserId,mCurrentPhotoPath;
    private TextView userFullname, userLastseen;
    private CircleImageView profileDp,deleteButoon;
    private ImageView backbutton,videocall,phonecall,more;
    private ImageButton sendmsgButton,attachfile,imagesend,videosend,filesend,audiosend,micbutton,capturebutton;
    private EditText msgInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private final List<message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private message_adapter messageAdapter;
    private RecyclerView usermsglist;
    private String savecurrentDate,savecurrentTime,checker = "",myUrl = "",buttonStatus = "NonRecording";
    private Uri imagefile;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;
    private FrameLayout frameLayout,audioframelayout;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private Bitmap mImageBitmap;
    private MediaPlayer MmediaPlayer;
    private String AudioSavePathInDevice = null;
    private MediaRecorder mediaRecorder ;
    private Random random ;
    private String RandomAudioFileName = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static final int RequestPermissionCode = 1;



    int id = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chat);
        random = new Random();
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        msgReciverId = getIntent().getExtras().get("userIds").toString();
        msgReciverName = getIntent().getExtras().get("fullname").toString();

        //Toast.makeText(personalChat.this,"cyrrentuserid" + CurrentUserId + "reciverid" + msgReciverId,Toast.LENGTH_SHORT).show();
       // msgReciverProfileImage = getIntent().getExtras().get("image").toString();
        intialization();
        recyclermethod();
        edittextmethod();
        toolbarsetup();
         attachfilemethod();
         sendmediafile();
        //Picasso.get().load(msgReciverProfileImage).into(profileDp);
        sendmsessage();
        backbuttonmethod();


    }

    private void backbuttonmethod() {
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
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

    private void edittextmethod() {

        int displaywidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int displayhight = Resources.getSystem().getDisplayMetrics().heightPixels;
        if(displayhight > displaywidth){
            msgInputText.getLayoutParams().width = displaywidth - 320;
            edittextportionclickable();
        }else{
            msgInputText.getLayoutParams().width = displaywidth - 320;
        }

    }

    private void edittextportionclickable() {
        Drawable drawable = getResources().getDrawable(R.mipmap.camera);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.6),
                (int) (drawable.getIntrinsicHeight() * 0.6));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 40, 40);
        msgInputText.setCompoundDrawables(null, null, sd.getDrawable(), null);
        msgInputText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if (event.getRawX() >= (msgInputText.getRight()- msgInputText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())){
                        cameraMethod();
                        return true;
                    }
                }
                return false;
            }
        });
    }


    private void cameraMethod() {
        boolean data = checkCameraHardware(personalChat.this);
        if (data == true){
            checker = "imageCapture";
            //Toast.makeText(personalChat.this,"Camera exits",Toast.LENGTH_SHORT).show();
            cameraopen(checker);
        }else{
            Toast.makeText(personalChat.this,"Camera doesn't exits",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendmediafile() {
        imagesend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "image";
                Intent imageIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imageIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(imageIntent,"Select Image"),438);
            }
        });
        videosend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "video";
                Intent videoIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                videoIntent.setType("video/*");
                startActivityForResult(Intent.createChooser(videoIntent,"Select Video"),438);
            }
        });
        audiosend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "audio";
                Intent audioIntent = new Intent();
                audioIntent.setAction(Intent.ACTION_GET_CONTENT);
                audioIntent.setType("audio/*");
                startActivityForResult(Intent.createChooser(audioIntent,"Select Audio"),438);
            }
        });
        filesend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "pdf";
                Intent pdfIntent = new Intent();
                pdfIntent.setAction(Intent.ACTION_GET_CONTENT);
                pdfIntent.setType("application/pdf/*");
                startActivityForResult(Intent.createChooser(pdfIntent,"Select pdf file"),438);
            }
        });
        micbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              audiomethod();
              frameLayout.setVisibility(View.GONE);

              //validateMicAvailability();
              audiorecordermethod();
            }
        });


    }

    private boolean validateMicAvailability(){
        Boolean available = true;
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                available = false;

            }

            recorder.startRecording();
            //Toast.makeText(personalChat.this,"recording",Toast.LENGTH_SHORT).show();
            //audiorecordermethod();

            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
                available = false;

            }
            recorder.stop();
        } finally{
            recorder.release();
            recorder = null;
        }

        return available;
    }

    private void audiorecordermethod(){
       if(checkPermission()){
           capturebutton.setOnTouchListener(new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   String filepath = null;
                   String lastname = null;
                   if(event.getAction() == MotionEvent.ACTION_DOWN){
                       validateMicAvailability();
                       AudioSavePathInDevice =
                               Environment.getExternalStorageDirectory().getAbsolutePath() + "/Alpha/Voice/Send/" ;
                       File audiodir = new File(AudioSavePathInDevice);
                       if (!audiodir.exists()){
                           audiodir.mkdirs();
                       }
                       lastname = CreateRandomAudioFileName(10) + ".3gp";
                       filepath = audiodir + "/"+ lastname;
                       MediaRecorderReady(filepath);

                       try {
                           mediaRecorder.prepare();
                           mediaRecorder.start();
                       } catch (IllegalStateException e) {
                           // TODO Auto-generated catch block
                           e.printStackTrace();
                       } catch (IOException e) {
                           // TODO Auto-generated catch block
                           e.printStackTrace();
                       }

                       return true;
                   }else if(event.getAction() == MotionEvent.ACTION_UP){

                       try{
                           mediaRecorder.stop();
                           mediaRecorder.release();
                       }catch (Exception e){
                       }

                       File audiofiledata = new File(Environment.getExternalStorageDirectory()+"/Alpha/Voice/Send/"+lastname);
                      if(audiofiledata.exists()){
                          Uri voicefile = Uri.fromFile(audiofiledata);
                          uploadtoserver(voicefile,lastname);
                      }else{
                          Toast.makeText(personalChat.this,"Error ouccur while sending data",Toast.LENGTH_SHORT).show();
                      }
                       return true;
                   }
                   return false;
               }
           });
       }else{
           requestPermission();
       }
    }

    private void uploadtoserver(Uri voicefile, final String filename) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Voice");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
        final String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
        final String msgPushId = usermsgKeyRef.getKey();
        final StorageReference filePath = imagereference.child(msgPushId+"."+"mp3");
        uploadTask = filePath.putFile(voicefile);
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
                    myUrl = downloadUri.toString();
                    checker = "voice";
                    uploadfiletotheserver(myUrl,filename,checker,msgPushId,msgSenderref,msgReciverref);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(personalChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                notificationbar(progress,checker);
            }
        });
    }
    private  void uploadfiletotheserver(String myUrl, String filename, String checker, final String msgPushId, final String msgSenderref, final String msgReciverref){
        Map msgpicbody = new HashMap();
        msgpicbody.put("message",myUrl);
        msgpicbody.put("name",filename);
        msgpicbody.put("type",checker);
        msgpicbody.put("ffrom",mAuth.getCurrentUser().getUid());
        msgpicbody.put("to",msgReciverId);
        msgpicbody.put("date", savecurrentDate);
        msgpicbody.put("time",savecurrentTime);
        msgpicbody.put("messageId",msgPushId);

        Map msgBodyDetail = new HashMap();
        msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgpicbody);
        msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgpicbody);

        rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);

                }else{
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);
                    Toast.makeText(personalChat.this,"error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void MediaRecorderReady(String filepath){
        mediaRecorder=new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(filepath);
            mediaRecorder.setAudioChannels(1);
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setAudioEncodingBitRate(44100);

    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(personalChat.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(personalChat.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(personalChat.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void onStop() {
        super.onStop();

        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (MmediaPlayer != null) {
            MmediaPlayer.release();
            MmediaPlayer = null;
        }
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
    }

    private void audiomethod() {
        int cx = (audioframelayout.getLeft() + audioframelayout.getRight()) / 2;
        int cy = (audioframelayout.getTop() + audioframelayout.getBottom()) / 2;

        int dx = Math.max(cx, audioframelayout.getWidth() - cx);
        int dy = Math.max(cy, audioframelayout.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        if(audioframelayout.getVisibility() == View.GONE){
            audioframelayout.setVisibility(View.VISIBLE);
            ViewAnimationUtils.createCircularReveal(audioframelayout,cx,cy,0,finalRadius).start();
        }else{
            Animator reveal = ViewAnimationUtils.createCircularReveal(audioframelayout,cx,cy,0,finalRadius);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    audioframelayout.setVisibility(View.GONE);
                }
            });
            reveal.start();
        }
    }

    private void cameraopen(String checker) {
        Camera c = null;
        if(c == null) {
            try {
                Intent cameraIntent = new Intent(personalChat.this,cameraintent.class);
                cameraIntent.putExtra("Checker",checker);
                cameraIntent.putExtra("userIds",msgReciverId);
                cameraIntent.putExtra("fullname",msgReciverName);
                cameraIntent.putExtra("image",ProfileImage);
                startActivity(cameraIntent);
                 // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                c.release();
                c = null;
                Toast.makeText(personalChat.this, "Error:" + e, Toast.LENGTH_SHORT).show();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imagefile = data.getData();
            String filePath = this.getRealPathFromURI(imagefile);
            Long Filelength = this.getfilesize(filePath);
            if(checckfilesize(Filelength).equals("ok")) {
                if (checker.equals("pdf")) {
                    pdfFilemessage(filePath);
                } else if (checker.equals("image")) {
                    String imagefilepath = compressImage(filePath);
                    Uri imageuriaftercompress = Uri.fromFile(new File(imagefilepath));
                    //Toast.makeText(personalChat.this, imagefilepath, Toast.LENGTH_SHORT).show();
                    String imagefileName = imageuriaftercompress.getLastPathSegment();
                    imagefilemessage(imageuriaftercompress,imagefileName);

                } else if (checker.equals("video")) {
                    String fileName = this.getfilename(filePath);

                   videofilemessage(imagefile,filePath,fileName);

                }
                else if(checker.equals("audio")){
                    String fileName = this.getfilename(filePath);
                   audiofilemessage(filePath,imagefile,fileName);
                }
            }else{
                Toast.makeText(personalChat.this,checckfilesize(Filelength),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getfilename(String filepath){
        File file = new File(filepath);
        String filename = file.getName();
        return filename;
    }

    private void audiofilemessage(String vfilePath, final Uri imagefile,final String filename) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Audio");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
        final String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
        final String msgPushId = usermsgKeyRef.getKey();
        final StorageReference filePath = imagereference.child(msgPushId+"."+"mp3");
        uploadTask = filePath.putFile(imagefile);
        String destinationfile = Environment.getExternalStorageDirectory()+"/Alpha/Audio/Send/"+msgPushId+".mp3";
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
                    myUrl = downloadUri.toString();

                    uploadfiletotheserver(myUrl,filename,checker,msgPushId,msgSenderref,msgReciverref);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(personalChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                notificationbar(progress,checker);
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
    public String checckfilesize(Long Filelength){
        String checkstate;
        if(Filelength < 16000 ){
            checkstate = "ok";
        }else{
            checkstate = "file large than 16mb.";
        }
        return checkstate;
    }
    public Long getfilesize(String filepath){
        File filedata = new File(filepath);
        Long fileLength = filedata.length();
        fileLength = fileLength/1024;
        return fileLength;
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
//        Toast.makeText(personalChat.this, Long.toString(cursor.getLong(sizeIndex)),Toast.LENGTH_SHORT).show();
        return cursor.getString(column_index);
    }

    public String compressImage(String filePath) {

        String compressfilePath = filePath;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(compressfilePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(compressfilePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(compressfilePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Alpha/Photos");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

   private void  videofilemessage(final Uri imagefile, final String vfilePath,final String filename){
       StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Video");
       final String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
       final String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
       DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
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
                   myUrl = downloadUri.toString();

                   uploadvideoTempfile(myUrl,filename,checker,msgPushId,msgSenderref,msgReciverref);

               }
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               loadingBar.dismiss();
               Toast.makeText(personalChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
           }
       });
       uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
               double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
               notificationbar(progress,checker);
           }
       });
   }
   private void notificationbar(double progress,String type){
        final int value = (int) progress;
       notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       mBuilder = new NotificationCompat.Builder(this);
       mBuilder.setContentTitle("Message sending")
               .setContentText("sending"+type+"....")
               .setPriority(NotificationCompat.PRIORITY_DEFAULT);

               //.setSmallIcon(R.drawable.download);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                mBuilder.setProgress(100,value,false);
                if(value == 100){
                    mBuilder.setProgress(0,0,false)
                            .setContentText("Message send");
                }
                notificationManager.notify(id,mBuilder.build());
            }
        }).start();
   }
    private void uploadvideoTempfile(final String myUrl,final String filename,final String checker, final String msgPushId, final String msgSenderref, final String msgReciverref) {
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
                    uploaddatatotheserver(myUrl,filename,checker,msgPushId,msgSenderref,msgReciverref,myfileUrl);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(personalChat.this,"Error:"+ e,Toast.LENGTH_SHORT).show();
            }
        });
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
    private void imagefilemessage(final Uri imagefile, final String filename) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Image");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
        final String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
        final String msgPushId = usermsgKeyRef.getKey();
        final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
        uploadTask = filePath.putFile(imagefile);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUrl = downloadUri.toString();

                    uploadfiletotheserver(myUrl,filename,checker,msgPushId,msgSenderref,msgReciverref);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(personalChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                notificationbar(progress,checker);
            }
        });
    }

    private void pdfFilemessage(String vfilePath) {

        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Doc");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
        final String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
        final String msgPushId = usermsgKeyRef.getKey();
        reciverpdftempImage(imagefile,msgPushId);
        final StorageReference filePath = imagereference.child(msgPushId+"."+"pdf");
        uploadTask = filePath.putFile(imagefile);
        String destinationfile = Environment.getExternalStorageDirectory()+"/Alpha/pdf/Send/"+msgPushId+".mp4";
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
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUrl = downloadUri.toString();
                    uploadpdfTempfile(myUrl,imagefile,checker,msgPushId,msgSenderref,msgReciverref);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(personalChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                notificationbar(progress,checker);
            }
        });
    }



    private void uploaddatatotheserver(String myUrl, String filename, String checker, final String msgPushId, final String msgSenderref, final String msgReciverref,final String myfileurl) {


        Map msgpicbody = new HashMap();
        msgpicbody.put("message",myUrl);
        msgpicbody.put("name",filename);
        msgpicbody.put("type",checker);
        msgpicbody.put("ffrom",CurrentUserId);
        msgpicbody.put("to",msgReciverId);
        msgpicbody.put("date", savecurrentDate);
        msgpicbody.put("time",savecurrentTime);
        msgpicbody.put("messageId",msgPushId);
        msgpicbody.put("tempfile",myfileurl);

        Map msgBodyDetail = new HashMap();
        msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgpicbody);
        msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgpicbody);

        rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);

                }else{
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);
                    Toast.makeText(personalChat.this,"error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void uploadpdfdatatotheserver(String myUrl, Uri imagefile, String checker, final String msgPushId, final String msgSenderref, final String msgReciverref,final String myfileurl) {


        Map msgpicbody = new HashMap();
        msgpicbody.put("message",myUrl);
        msgpicbody.put("name",imagefile.getLastPathSegment());
        msgpicbody.put("type",checker);
        msgpicbody.put("ffrom",CurrentUserId);
        msgpicbody.put("to",msgReciverId);
        msgpicbody.put("date", savecurrentDate);
        msgpicbody.put("time",savecurrentTime);
        msgpicbody.put("messageId",msgPushId);
        msgpicbody.put("tempfile",myfileurl);

        Map msgBodyDetail = new HashMap();
        msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgpicbody);
        msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgpicbody);

        rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);

                }else{
                    loadingBar.dismiss();
                    msgInputText.setText(null);
                    frameLayout.setVisibility(View.GONE);
                    Toast.makeText(personalChat.this,"error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void uploadpdfTempfile(final String myUrl,final Uri imagefile,final String checker, final String msgPushId, final String msgSenderref, final String msgReciverref) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Doc").child("temp");
        Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/Alpha/PDF/temp/"+msgPushId+".png"));
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
                    uploadpdfdatatotheserver(myUrl,imagefile,checker,msgPushId,msgSenderref,msgReciverref,myfileUrl);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(personalChat.this,"Error:"+ e,Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void reciverpdftempImage(Uri imagefile, String msgpushId) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(this);
        try {

            ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(imagefile, "r");
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            saveImage(bmp,msgpushId);
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch(Exception e) {
            //todo with exception
        }
    }

    public final static String FOLDER = Environment.getExternalStorageDirectory() + "/Alpha/PDF/temp";
    private void saveImage(Bitmap bmp, String msgpushId) {
        FileOutputStream out = null;
        try {
            File folder = new File(FOLDER);
            if(!folder.exists())
                folder.mkdirs();
            File file = new File(folder, msgpushId+".png");
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            String filepath = file.getPath();
            Log.d("path",filepath);
            // bmp is your Bitmap instance
        } catch (Exception e) {
            //todo with exception
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //todo with exception
            }
        }
    }

    private void attachfilemethod() {
        attachfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                int cx = (frameLayout.getLeft() + frameLayout.getRight()) / 2;
                int cy = (frameLayout.getTop() + frameLayout.getBottom()) / 2;

                int dx = Math.max(cx, frameLayout.getWidth() - cx);
                int dy = Math.max(cy, frameLayout.getHeight() - cy);
                float finalRadius = (float) Math.hypot(dx, dy);

                if(frameLayout.getVisibility() == View.GONE){
                    frameLayout.setVisibility(View.VISIBLE);
                    ViewAnimationUtils.createCircularReveal(frameLayout,cx,cy,0,finalRadius).start();
                }else{
                    Animator reveal = ViewAnimationUtils.createCircularReveal(frameLayout,cx,cy,0,finalRadius);
                    reveal.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            frameLayout.setVisibility(View.GONE);
                        }
                    });
                    reveal.start();
                }
            }
        });
    }



    private void toolbarsetup() {
        userFullname.setText(msgReciverName);
        rootRef.child("User").child(msgReciverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.hasChild("image")){
                   ProfileImage = dataSnapshot.child("image").getValue().toString();
                   Picasso.get().load(ProfileImage).placeholder(R.drawable.im).into(profileDp);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        onlineStatus();
        profileDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileViewIntent = new Intent(getApplicationContext(),profileView.class);
                profileViewIntent.putExtra("userId", msgReciverId);
                startActivity(profileViewIntent);
            }
        });
        videocall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileViewIntent = new Intent(getApplicationContext(),videocalling.class);
                profileViewIntent.putExtra("currentuser",CurrentUserId);
                startActivity(profileViewIntent);
            }
        });
    }

    private void onlineStatus() {
        rootRef.child("User").child(msgReciverId).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.hasChild("State")){
                  String state = dataSnapshot.child("State").getValue().toString();
                  String date = dataSnapshot.child("Date").getValue().toString();
                  String time = dataSnapshot.child("Time").getValue().toString();

                  Calendar calendar = Calendar.getInstance();
                  SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                  String savecurrentDate = currentDate.format(calendar.getTime());
                  if (state.equals("online")){
                      userLastseen.setText("Online");
                  }else if(savecurrentDate.equals(date)){
                      userLastseen.setText("Last seen"+time);
                  }else{
                      userLastseen.setText("Last seen"+ time + date);
                  }
              }else {
                  userLastseen.setText("Offline");
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void recyclermethod() {
        rootRef.child("Message").child(CurrentUserId).child(msgReciverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
              message cMessage = dataSnapshot.getValue(message.class);

              messageList.add(cMessage);
              messageAdapter.notifyDataSetChanged();
              usermsglist.smoothScrollToPosition(usermsglist.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                message cMessage = dataSnapshot.getValue(message.class);

                messageList.add(cMessage);
                messageAdapter.notifyDataSetChanged();
                usermsglist.smoothScrollToPosition(usermsglist.getAdapter().getItemCount());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendmsessage() {
        sendmsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String msgText = msgInputText.getText().toString();
             if (msgText.isEmpty()){
                 Toast.makeText(personalChat.this,"Please enter some Text to send",Toast.LENGTH_SHORT).show();
             }else{
                 String msgSenderref = "Message/" + CurrentUserId + "/" + msgReciverId;
                 String msgReciverref = "Message/" + msgReciverId + "/" + CurrentUserId;
                 DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(msgReciverId).push();
                 String msgPushId = usermsgKeyRef.getKey();

                 Map msgTxtbody = new HashMap();
                 msgTxtbody.put("message",msgText);
                 msgTxtbody.put("type","Text");
                 msgTxtbody.put("ffrom",CurrentUserId);
                 msgTxtbody.put("to",msgReciverId);
                 msgTxtbody.put("date", savecurrentDate);
                 msgTxtbody.put("time",savecurrentTime);
                 msgTxtbody.put("messageId",msgPushId);

                 Map msgBodyDetail = new HashMap();
                 msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgTxtbody);
                 msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgTxtbody);

                 rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                     @Override
                     public void onComplete(@NonNull Task task) {
                       if (task.isSuccessful()){
                           msgInputText.setText(null);
                       }else{
                           msgInputText.setText(null);
                           Toast.makeText(personalChat.this,"error",Toast.LENGTH_SHORT).show();
                       }

                     }
                 });
             }
            }
        });
    }

    private void intialization() {
        sendmsgButton = (ImageButton) findViewById(R.id.send);
        micbutton = (ImageButton) findViewById(R.id.mic);
        attachfile = (ImageButton) findViewById(R.id.attach);
        imagesend = (ImageButton) findViewById(R.id.image);
        videosend= (ImageButton) findViewById(R.id.video);
        filesend = (ImageButton) findViewById(R.id.Document);
        audiosend = (ImageButton) findViewById(R.id.audio);
        msgInputText = (EditText) findViewById(R.id.msgInput);
        frameLayout = (FrameLayout) findViewById(R.id.framelayout);
        audioframelayout = (FrameLayout) findViewById(R.id.audioframelayout);
        //setSupportActionBar(chatToolbar);
        profileDp = (CircleImageView) findViewById(R.id.customProfileImage);
        backbutton = findViewById(R.id.backbuttonchat);
        videocall = findViewById(R.id.videocall);
        phonecall = findViewById(R.id.Audiocall);
        more = findViewById(R.id.more);
        userFullname = (TextView) findViewById(R.id.Customeusername);
        userLastseen = (TextView) findViewById(R.id.CustomeLastseen);
        messageAdapter = new message_adapter(messageList);
        usermsglist = (RecyclerView) findViewById(R.id.msg_list_user);
        linearLayoutManager = new LinearLayoutManager(this);
        usermsglist.setLayoutManager(linearLayoutManager);
        usermsglist.setAdapter(messageAdapter);
        usermsglist.setHasFixedSize(true);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());

        loadingBar = new ProgressDialog(this);


        capturebutton = (ImageButton) findViewById(R.id.capture);

    }
}

