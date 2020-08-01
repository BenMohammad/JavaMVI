package com.benmohammad.javamvi.injection;

import android.content.Context;

import androidx.annotation.NonNull;

import com.benmohammad.javamvi.data.source.TasksRepository;
import com.benmohammad.javamvi.data.source.local.TasksLocalDataSource;
import com.benmohammad.javamvi.data.source.remote.TasksRemoteDataSource;
import com.benmohammad.javamvi.util.schedulers.BaseSchedulerProvider;
import com.benmohammad.javamvi.util.schedulers.SchedulerProvider;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class Injection {

    public static TasksRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        return TasksRepository.getInstance(TasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}
