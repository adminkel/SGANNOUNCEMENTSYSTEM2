package com.example.sgannouncementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    List<Model> modelList = new ArrayList<>();
    RecyclerView mRecyclerView;

    RecyclerView.LayoutManager layoutManager;

    Toolbar toolbar;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    StudentAdapter adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionButton mfab_menu, mfab_contact, mfab_about, mfab_flag;
    Animation fabOpen, fabClose, fabRClockwise, fabCClockwise;

    private ProgressDialog pd;

    private boolean InternetCheck = true;

    boolean isOpen = false;

    static String LoggedIn_User_Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        LoggedIn_User_Email = user.getEmail();

        OneSignal.sendTag("User_ID", LoggedIn_User_Email);

        signOut();

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Loading Announcements...",Snackbar.LENGTH_SHORT);
        snackbar.show();

        db = FirebaseFirestore.getInstance();

        pd = new ProgressDialog(this);

        mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green_tertiary);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (swipeRefreshLayout != null){
                    swipeRefreshLayout.setRefreshing(true);
                    showData();
                }
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mfab_menu = (FloatingActionButton)findViewById(R.id.fab_menu);
        mfab_contact = (FloatingActionButton)findViewById(R.id.fab_contact);
        mfab_about = (FloatingActionButton)findViewById(R.id.fab_about);
        mfab_flag = (FloatingActionButton)findViewById(R.id.fab_flag);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fabRClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        fabCClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_counter_clockwise);

        mfab_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen){

                    mfab_contact.startAnimation(fabClose);
                    mfab_about.startAnimation(fabClose);
                    mfab_flag.startAnimation(fabClose);
                    mfab_menu.startAnimation(fabCClockwise);
                    mfab_flag.setClickable(false);
                    mfab_about.setClickable(false);
                    mfab_contact.setClickable(false);
                    isOpen = false;

                }else {

                    mfab_contact.startAnimation(fabOpen);
                    mfab_about.startAnimation(fabOpen);
                    mfab_flag.startAnimation(fabOpen);
                    mfab_menu.startAnimation(fabRClockwise);
                    mfab_flag.setClickable(true);
                    mfab_about.setClickable(true);
                    mfab_contact.setClickable(true);
                    isOpen = true;
                }
            }
        });

        mfab_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StudentActivity.this, ContactActivity.class);
                startActivity(i);
            }
        });

        mfab_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StudentActivity.this, AboutActivity.class);
                startActivity(i);
            }
        });
    }

    private void showData() {
        swipeRefreshLayout.setRefreshing(true);


        db.collection("Announcements").orderBy("Timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for (DocumentSnapshot doc : task.getResult()) {
                            Model model = new Model(doc.getString("id"),
                                    doc.getString("Announcement Title"),
                                    doc.getString("Announcement Details"),
                                    doc.getString("Admin"),
                                    doc.getDate("Timestamp"));
                            modelList.add(model);
                        }

                        adapter = new StudentAdapter(StudentActivity.this, modelList);

                        mRecyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(StudentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void clearData(){
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setBackgroundResource(R.drawable.search_background);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchData(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchDataAuto(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void searchData(String s) {
        pd.setMessage("Searching...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        db.collection("Announcements").orderBy("Search")
                .startAt(s)
                .endAt(s + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        modelList.clear();
                        pd.dismiss();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Model model = new Model(doc.getString("id"),
                                    doc.getString("Announcement Title"),
                                    doc.getString("Announcement Details"),
                                    doc.getString("Admin"),
                                    doc.getDate("Timestamp"));
                            modelList.add(model);
                        }

                        adapter = new StudentAdapter(StudentActivity.this, modelList);
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(StudentActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void searchDataAuto(String s) {

        db.collection("Announcements").orderBy("Search")
                .startAt(s)
                .endAt(s + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        modelList.clear();

                        for (DocumentSnapshot doc : task.getResult()) {
                            Model model = new Model(doc.getString("id"),
                                    doc.getString("Announcement Title"),
                                    doc.getString("Announcement Details"),
                                    doc.getString("Admin"),
                                    doc.getDate("Timestamp"));
                            modelList.add(model);
                        }

                        adapter = new StudentAdapter(StudentActivity.this, modelList);
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(StudentActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_admin:
                showLogin();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onRefresh() {
        checkConnection();
    }

    private void showLogin() {
        Intent intent = new Intent(StudentActivity.this,LoginActivity.class);
        startActivity(intent);
    }


    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            swipeRefreshLayout.setRefreshing(false);
            return true;
        } else {
            swipeRefreshLayout.setRefreshing(false);
            return false;
        }
    }

    public boolean checkConnection() {
        if (isOnline()){
            clearData();
            showData();
            return InternetCheck;
        }else {
            InternetCheck=false;
            Toast.makeText(StudentActivity.this,"Problem reloading data:\nPlease check your Internet connection.",
                    Toast.LENGTH_SHORT).show();
            return InternetCheck;
        }
    }
}
