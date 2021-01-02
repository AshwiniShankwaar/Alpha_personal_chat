package com.nanb.alpha;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class createFragment extends Fragment {

    private View view;
    private CircleImageView cancel;
    private EditText editText;
    private FirebaseAuth mAuth;
    private String currentuserid,currenttime,currentdate,path;
    private DatabaseReference rootref;
    private Button createnextbutton;
    private ImageButton color,font;
    private String posttext;
    private LinearLayout viewlinear;
    //private ImageView postpreview;
    private String bgcolorsubstring="",fontsubstring="";
    ArrayList<Integer> bgcolorlist = new ArrayList<Integer>(11);
    ArrayList<Integer> fontstyle = new ArrayList<Integer>(7);
    int fontpostion = 0;
    int postion = 0;
    public createFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_create, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm");
        currenttime = sdf.format(new Date());
        SimpleDateFormat sdformat = new SimpleDateFormat(" yyyy.MM.dd");
        currentdate = sdformat.format(new Date());

        Intialization();
        edittextpostmethod();
        backbuttonmethod();
       // postmethod();
        postpreviewmethod();
        backgroundcolormethod();
        fontstylemethod();

        return view;
    }


    private void edittextpostmethod() {
        posttext = editText.getText().toString();

    }


    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
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
    private void fontstylemethod() {
        fontstyle.add(R.font.abril_fatface);
        fontstyle.add(R.font.cedarville_cursive);
        fontstyle.add(R.font.alfa_slab_one);
        fontstyle.add(R.font.annie_use_your_telescope);
        fontstyle.add(R.font.aclonica);
        fontstyle.add(R.font.aguafina_script);
        fontstyle.add(R.font.arizonia);

        Typeface defaulttypeface = ResourcesCompat.getFont(view.getContext(),fontstyle.get(0));

        editText.setTypeface(defaulttypeface);

        font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fontpostion == 6){
                    //Toast.makeText(view.getContext(),"last element",Toast.LENGTH_SHORT).show();
                    Typeface ttypeface = ResourcesCompat.getFont(view.getContext(),fontstyle.get(fontpostion));
                    editText.setTypeface(ttypeface);
                    fontpostion= 0;
                }else{
                    fontpostion++;
                    Typeface ttypeface = ResourcesCompat.getFont(view.getContext(),fontstyle.get(fontpostion));
                    editText.setTypeface(ttypeface);
                    //editText.setText(fontstyle.get(fontpostion));
                    //Toast.makeText(view.getContext(), (CharSequence) ResourcesCompat.getFont(view.getContext(),fontstyle.get(fontpostion)),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void backgroundcolormethod() {

        bgcolorlist.add(R.color.white);
        bgcolorlist.add(R.color.darkGreen);
        bgcolorlist.add(R.color.colorPrimaryDark);
        bgcolorlist.add(R.color.colorAccent);
        bgcolorlist.add(R.color.colorPrimary);
        bgcolorlist.add(R.color.orange);
        bgcolorlist.add(R.color.edittextbg5);
        bgcolorlist.add(R.color.edittextbg4);
        bgcolorlist.add(R.color.edittextbg3);
        bgcolorlist.add(R.color.edittextbg2);
        bgcolorlist.add(R.color.edittextbg1);

        editText.setBackgroundColor(getResources().getColor(bgcolorlist.get(0)));

        color.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                if(postion == 10){
                    //Toast.makeText(view.getContext(),"last element",Toast.LENGTH_SHORT).show();
                    editText.setBackgroundColor(getResources().getColor(bgcolorlist.get(postion)));
                    postion = 0;
                }else{
                    postion++;
                    editText.setBackgroundColor(getResources().getColor(bgcolorlist.get(postion)));
                    //editText.setText(bgcolorlist.get(postion));
                  // Toast.makeText(view.getContext(),getResources().getColor(bgcolorlist.get(postion)),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void postpreviewmethod() {
        createnextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(false);
                Bitmap screenShot = takescreenshot(editText);
                storeScreenshot(screenShot);
                Intent previewIntent = new Intent(view.getContext(),createpostpreview.class);
                previewIntent.putExtra("Path",path);
                startActivity(previewIntent);

            }
        });
    }

    public Bitmap takescreenshot(View rootView){
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(),rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }
    public String storeScreenshot(Bitmap bitmap) {
        Random generator = new Random();
        int n = 100000000;
        n = generator.nextInt(n);

        path = Environment.getExternalStorageDirectory().toString() +"/Alpha/Post/Post_"+n+".jpg" ;
        OutputStream out = null;
        File imageFile = new File(path);

        try {
            out = new FileOutputStream(imageFile);
            // choose JPEG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
        } catch (FileNotFoundException e) {
            // manage exception ...
        } catch (IOException e) {
            // manage exception ...
        } finally {

            try {
                if (out != null) {
                    out.close();
                }

            } catch (Exception exc) {
            }

        }
        return path;
    }
    private void backbuttonmethod(){
       cancel.setImageResource(R.mipmap.close);
       cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent mainIntent = new Intent(view.getContext(),MainActivity.class);
               startActivity(mainIntent);
           }
       });
    }

    private void Intialization() {
        cancel = view.findViewById(R.id.backbutton);
        createnextbutton = view.findViewById(R.id.nextbutton);
        editText = view.findViewById(R.id.textpost);
        color = view.findViewById(R.id.bgcolorbutton);
        font = view.findViewById(R.id.fontstyle);
        viewlinear = view.findViewById(R.id.linearview);
        //postpreview = view.findViewById(R.id.postcreatepreview);
    }

}
