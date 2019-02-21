package com.example.sgannouncementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "AdminActivity";

    private boolean InternetCheck = true;

    private ProgressDialog pd;

    List<Model> modelList = new ArrayList<>();
    RecyclerView mRecyclerView;
    AdminAdapter adapter;

    RecyclerView.LayoutManager layoutManager;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                }
                showData();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundColor(Color.parseColor("#FB7125"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

                        adapter = new AdminAdapter(AdminActivity.this, modelList);

                        mRecyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    boolean twice = false;

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        }else if (twice == true){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        twice = true;

        Toast.makeText(AdminActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
            }
        }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin, menu);

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

                        adapter = new AdminAdapter(AdminActivity.this, modelList);
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

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

                        adapter = new AdminAdapter(AdminActivity.this, modelList);
                        mRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(AdminActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            connectionCheck();
        }else if (id == R.id.action_changePass){
            changePassDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void changePassDialog(){
        ChangePassword changePassword = new ChangePassword();
        changePassword.show(getSupportFragmentManager(), "ChangePassword Dialog");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_compose) {
            Intent i = new Intent(AdminActivity.this, ComposeActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_edit) {

        } else if (id == R.id.nav_delete) {
            Intent i = new Intent(AdminActivity.this, DeleteActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_VM) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_contact) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        Log.d(TAG,"signOut: signing out");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(AdminActivity.this, LoggingoutActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean connectionCheck(){
        if (isOnline()){
            signOut();
            return InternetCheck;
        }else {
            InternetCheck=false;
            Snackbar.make(findViewById(android.R.id.content), "Please check your connection", Snackbar.LENGTH_SHORT).show();
            return InternetCheck;
        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            //swipeRefreshLayout.setRefreshing(false);
            return true;
        } else {
            //swipeRefreshLayout.setRefreshing(false);
            return false;
        }
    }

    @Override
    public void onRefresh() {
        checkConnection();
    }

    public boolean checkConnection() {
        if (isOnline()){
            clearData();
            showData();
            return InternetCheck;
        }else {
            InternetCheck=false;
            Toast.makeText(AdminActivity.this,"Problem reloading data:\nPlease check your Internet connection.",
                    Toast.LENGTH_SHORT).show();
            return InternetCheck;
        }
    }

    private void clearData() {
        adapter.clear();
    }
}
