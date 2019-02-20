package com.example.sgannouncementsystem;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

public class AnnouncementDisplay extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView aTitle, aAdmin, aDetails, aDate;

    String pId, pTitle, pAdmin, pDetails, pDate;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_display);

        aTitle = findViewById(R.id.Title);
        aAdmin = findViewById(R.id.Admin);
        aDetails = findViewById(R.id.Details);
        aDate = findViewById(R.id.Date);

        Bundle bundle = getIntent().getExtras();

        pId = bundle.getString("pId");
        pTitle = bundle.getString("pTitle");
        pAdmin = bundle.getString("pAdmin");
        pDetails = bundle.getString("pDetails");
        pDate = bundle.getString("pDate");

        aTitle.setText(pTitle);
        aAdmin.setText(pAdmin);
        aDetails.setText(pDetails);
        aDate.setText(pDate);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }
}
