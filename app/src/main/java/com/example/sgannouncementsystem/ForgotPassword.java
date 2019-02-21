package com.example.sgannouncementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText mEmail;
    private Button mReset;

    FirebaseAuth firebaseAuth;

    ProgressDialog pd;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmail = (EditText)findViewById(R.id.email);
        mReset = (Button)findViewById(R.id.btnReset);

        firebaseAuth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(this);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()){

                    pd.setMessage("Sending reset password email");
                    pd.setCanceledOnTouchOutside(false);
                    pd.setCancelable(false);
                    pd.show();

                    String adminemail = mEmail.getText().toString().trim();

                    if (adminemail==null){
                        pd.dismiss();
                        Toast.makeText(ForgotPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    }else {
                        firebaseAuth.sendPasswordResetEmail(adminemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    pd.dismiss();
                                    Toast.makeText(ForgotPassword.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    pd.dismiss();
                                    Toast.makeText(ForgotPassword.this, "Email not registered", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }else{
                    DialogAppear();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
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
