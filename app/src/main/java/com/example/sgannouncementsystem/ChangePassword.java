package com.example.sgannouncementsystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatDialogFragment {

    private EditText mPassword;
    private EditText cmPassword;
    private Button mChange;

    private ProgressDialog pd;

    private boolean InternetCheck = true;

    FirebaseAuth auth;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_password, null);

        mPassword = view.findViewById(R.id.cPassword);
        cmPassword = view.findViewById(R.id.cVPassword);
        mChange = view.findViewById(R.id.btnCPassword);

        auth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(getActivity());

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()){

                    pd.setMessage("Changing Password");
                    pd.show();

                    String password = mPassword.getText().toString();
                    String cpassword = cmPassword.getText().toString();

                    if (TextUtils.isEmpty(password)){
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Please input new password", Toast.LENGTH_SHORT).show();
                    }else if (TextUtils.isEmpty(cpassword)){
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Please confirm your password" , Toast.LENGTH_SHORT).show();
                    }else if (!password.equals(cpassword)){
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Password do not match", Toast.LENGTH_SHORT).show();
                    }else if (isOnline()){
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user!=null){
                            user.updatePassword(mPassword.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "Password has been changed", Toast.LENGTH_SHORT).show();
                                                auth.signOut();
                                                getActivity().finish();
                                                Intent i = new Intent(getActivity(), LoggingoutActivity.class);
                                                startActivity(i);
                                            }else {
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "Failed on changing password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }else {
                        checkConnection();
                    }
                } else {
                    DialogAppear();
                }

            }
        });

        builder.setView(view);
        return builder.create();
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
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        } else {
            return false;
        }
    }

    public void DialogAppear(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
