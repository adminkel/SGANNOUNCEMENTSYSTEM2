package com.example.sgannouncementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class FlashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    private int Sleep = 2;
    private boolean InternetCheck = true;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_flash_screen);

        pb = findViewById(R.id.progressBarFs);
        pb.setVisibility(View.VISIBLE);
        postDelayedMethod();

        Logo logoLauncher = new Logo();
        logoLauncher.start();
    }

    private class Logo extends Thread {
        public void run() {
            try {
                sleep(1000 * Sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void postDelayedMethod() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean InternetResult = checkConnection();
                if (InternetResult){
                    Intent intent = new Intent(FlashScreen.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    pb.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);

                    DialogAppear();
                }
            }
        },SPLASH_TIME_OUT);

    }

    private void DialogAppear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FlashScreen.this);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setTitle("No internet connection");
        dialog.setMessage("Please try again later");

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(FlashScreen.this, FlashScreen.class);
                startActivity(intent);
                finish();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.show();

    }

    public boolean checkConnection() {
        if (isOnline()){
            return InternetCheck;
        }else {
            InternetCheck=false;
            Toast.makeText(FlashScreen.this,"Please connect to internet", Toast.LENGTH_SHORT).show();
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

    boolean twice = false;

    @Override
    public void onBackPressed() {

        if (twice == true) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        twice = true;

        Toast.makeText(FlashScreen.this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
            }
        }, 3000);
    }

}
