package com.example.sgannouncementsystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class FabCompose extends AppCompatDialogFragment {

    private EditText mTitle;
    private EditText mDetails;
    private Button mPost;

    private ProgressDialog pd;

    private boolean InternetCheck = true;

    FirebaseFirestore db;

    FirebaseAuth mAuth;

    FirebaseUser mUser;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fab_compose, null);

        mTitle = view.findViewById(R.id.Title);
        mDetails = view.findViewById(R.id.Details);
        mDetails.setScroller(new Scroller(getActivity()));
        mDetails.setVerticalScrollBarEnabled(true);
        mDetails.setMaxLines(20);
        mDetails.setMovementMethod(new ScrollingMovementMethod());
        mPost = view.findViewById(R.id.btnPost);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        pd = new ProgressDialog(getActivity());

        db = FirebaseFirestore.getInstance();

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString();
                String details = mDetails.getText().toString().trim();

                if (title.isEmpty()|| details.isEmpty() ){
                    Toast.makeText(getActivity(),"Please fill the empty fields",Toast.LENGTH_SHORT).show();
                }else if (isOnline()){
                    uploadData(title, details);
                    sendNotification();
                }
                else {
                    checkConnection();
                }
            }
        });

        builder.setView(view);
        return builder.create();
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
        doc.put("Search",title.toLowerCase());
        doc.put("Announcement Details", details);

        db.collection("Announcements").document(id).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),"Announcement Posted",Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification()
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_email;

                    //This is a Simple Logic to Send Notification different Device Programmatically....
                    if (AdminActivity.LoggedIn_User_Email.equals(mUser.getEmail())) {
                        send_email = "student@email.com";
                    } else {
                        send_email = mUser.getEmail() ;
                    }

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic NGUzMmRjNzUtNzdhZC00YjE3LThlMGMtODFlM2MwNDMxNDY0");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"705c47c3-d8c0-46b6-94b8-4ec95f2f784b\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"contents\": {\"en\": \"Please check new Announcement\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
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
