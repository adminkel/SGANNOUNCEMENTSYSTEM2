package com.example.sgannouncementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComposeActivity extends AppCompatActivity {

    private EditText cTitle;
    private EditText cDetails;
    private Button cPost;

    private ProgressDialog pd;

    private boolean InternetCheck = true;

    Toolbar toolbar;

    FirebaseFirestore db;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        cTitle = findViewById(R.id.Title);
        cDetails = findViewById(R.id.Details);
        cDetails.setScroller(new Scroller(this));
        cDetails.setVerticalScrollBarEnabled(true);
        cDetails.setMaxLines(20);
        cPost = findViewById(R.id.btnPost);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setIcon(getDrawable(R.drawable.icon_compose));
        }

        pd = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();

        cPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = cTitle.getText().toString();
                String details = cDetails.getText().toString().trim();

                if (title.isEmpty()|| details.isEmpty() ){
                    Toast.makeText(ComposeActivity.this,"Please fill the empty fields",Toast.LENGTH_SHORT).show();
                }else if (isOnline()){
                    uploadData(title, details);
                    //sendNotification();
                }
                else {
                    checkConnection();
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadData(String title, String details) {
        pd.setMessage("Posting Announcement...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        String id = UUID.randomUUID().toString();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("Timestamp", FieldValue.serverTimestamp());
        doc.put("Admin", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        doc.put("Announcement Title", title);
        doc.put("Search", title.toLowerCase());
        doc.put("Announcement Details", details);

        db.collection("Announcements").document(id).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        Toast.makeText(ComposeActivity.this,"Announcement Posted",Toast.LENGTH_SHORT).show();
                        ComposeActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(ComposeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean checkConnection() {
        if (isOnline()){
            return InternetCheck;
        }else {
            InternetCheck=false;
            DialogAppear();
            return InternetCheck;
        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        } else {
            return false;
        }
    }

    public void DialogAppear(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeActivity.this);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setTitle("No internet connection!");
        dialog.setMessage("Please connect to internet.");

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        dialog.show();
    }
}
