package com.example.sgannouncementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.List;

public class DeleteAdapter extends RecyclerView.Adapter<ViewHolder> {

    DeleteActivity deleteActivity;
    List<Model> modelList;

    public DeleteAdapter(DeleteActivity deleteActivity, List<Model> modelList) {
        this.deleteActivity = deleteActivity;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.model_layout, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(itemView);

        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String id = modelList.get(position).getId();
                String title = modelList.get(position).getTitle();
                String admin = modelList.get(position).getAdmin();
                String details = modelList.get(position).getDetails();
                SimpleDateFormat spf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                String date = spf.format(modelList.get(position).getTime());

                Intent intent = new Intent(deleteActivity, AnnouncementDisplay.class);

                intent.putExtra("pId", id);
                intent.putExtra("pTitle", title);
                intent.putExtra("pAdmin", admin);
                intent.putExtra("pDetails", details);
                intent.putExtra("pDate", date);

                deleteActivity.startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(deleteActivity);
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                dialog.setMessage("Are you sure you want to delete the Announcement?");

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isOnline()){
                            deleteActivity.deleteData(position);
                        }else {
                            DialogAppear();
                        }

                    }
                });

                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });


                dialog.show();

            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.mTitle.setText(modelList.get(i).getTitle());
        viewHolder.mAdmin.setText(modelList.get(i).getAdmin());
        viewHolder.mDetails.setText(modelList.get(i).getDetails());
        SimpleDateFormat spf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String date = spf.format(modelList.get(i).getTime());
        viewHolder.mDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public  void clear(){
        modelList.clear();
        notifyDataSetChanged();
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)deleteActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            deleteActivity.swipeRefreshLayout.setRefreshing(false);
            return true;
        } else {
            deleteActivity.swipeRefreshLayout.setRefreshing(false);
            return false;
        }
    }

    public void DialogAppear(){
        AlertDialog.Builder builder = new AlertDialog.Builder(deleteActivity);
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
