package com.benmohammad.javamvi.taskdetail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.util.ActivityUtils;

public class TaskDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "TASK_ID";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        TaskDetailFragment taskDetailFragment = (TaskDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if(taskDetailFragment == null) {
            taskDetailFragment = TaskDetailFragment.newInstance(taskId);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), taskDetailFragment, R.id.contentFrame);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
