package com.example.sgannouncementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DeleteActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    List<Model> modelList = new ArrayList<>();
    RecyclerView mRecyclerView;

    RecyclerView.LayoutManager layoutManager;

    FirebaseFirestore db;

    DeleteAdapter adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    Toolbar toolbar;

    private ProgressDialog pd;

    private boolean InternetCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

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

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setIcon(getDrawable(R.drawable.icon_delete));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showData() {
        swipeRefreshLayout.setRefreshing(true);


        db.collection("Announcements").orderBy("Timestamp", Query.Direction.DESCENDING)
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

                        adapter = new DeleteAdapter(DeleteActivity.this, modelList);

                        mRecyclerView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(DeleteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void deleteData(int index){

        pd.setMessage("Deleting Announcement...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        db.collection("Announcements").document(modelList.get(index).getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(DeleteActivity.this, "Announcement Deleted", Toast.LENGTH_SHORT).show();
                        showData();
                        pd.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(DeleteActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void clearData(){
        adapter.clear();
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
            Toast.makeText(DeleteActivity.this,"Problem reloading data:\nPlease check your Internet connection.",
                    Toast.LENGTH_SHORT).show();
            return InternetCheck;
        }
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

}
