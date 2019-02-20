package com.example.sgannouncementsystem;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<ViewHolder> {

    StudentActivity studentActivity;
    List<Model> modelList;

    public StudentAdapter(StudentActivity studentActivity, List<Model> modelList) {
        this.studentActivity = studentActivity;
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

                Intent intent = new Intent(studentActivity, AnnouncementDisplay.class);

                intent.putExtra("pId", id);
                intent.putExtra("pTitle", title);
                intent.putExtra("pAdmin", admin);
                intent.putExtra("pDetails", details);
                intent.putExtra("pDate", date);

                studentActivity.startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int position) {

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
}
