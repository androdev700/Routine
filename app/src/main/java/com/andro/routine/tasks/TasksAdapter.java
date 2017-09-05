package com.andro.routine.tasks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andro.routine.R;

import java.util.ArrayList;

/**
 * Created by andro on 05/09/17.
 */

class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private ArrayList<String> tasks;

    TasksAdapter(ArrayList<String> tasks) {
        this.tasks = tasks;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_task;

        ViewHolder(View view) {
            super(view);
            tv_task = view.findViewById(R.id.listitem_text_to_do);
        }

        public void setData(int pos) {
            tv_task.setText(tasks.get(pos));
        }
    }

    @Override
    public TasksAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_task, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TasksAdapter.ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    void addItem(String country) {
        tasks.add(country);
        notifyItemInserted(tasks.size());
    }

    void removeItem(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasks.size());
    }


}