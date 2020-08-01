package com.benmohammad.javamvi.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.data.Task;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class TasksAdapter extends BaseAdapter {

    private PublishSubject<Task> taskClickObservable = PublishSubject.create();
    private PublishSubject<Task> taskToggleObservable = PublishSubject.create();
    private List<Task> tasks;

    public TasksAdapter(List<Task> tasks) {
        setList(tasks);
    }

    private void setList(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void replaceData(List<Task> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }


    Observable<Task> getTaskClickObservable() {
        return taskClickObservable;
    }

    Observable<Task> getTAskToggleObservable() {
        return taskToggleObservable;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            rowView = inflater.inflate(R.layout.task_item, parent, false);
        }

        final Task task = getItem(position);
        TextView titleTV = rowView.findViewById(R.id.title);
        titleTV.setText(task.getTitleForLIst());

        CheckBox completeCB = rowView.findViewById(R.id.complete);
        completeCB.setChecked(task.isCompleted());

        completeCB.setOnClickListener(ignored -> taskToggleObservable.onNext(task));
        rowView.setOnClickListener(ignored -> taskClickObservable.onNext(task));

        return rowView;

    }
}
