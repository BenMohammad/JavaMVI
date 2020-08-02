package com.benmohammad.javamvi.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.injection.Injection;
import com.benmohammad.javamvi.taskdetail.TaskDetailActionProcessorHolder;
import com.benmohammad.javamvi.taskdetail.TaskDetailViewModel;
import com.benmohammad.javamvi.tasks.TasksActionProcessorHolder;
import com.benmohammad.javamvi.tasks.TasksViewModel;

import java.util.function.ToDoubleBiFunction;

public class TodoViewModelFactory implements ViewModelProvider.Factory {


    private static TodoViewModelFactory INSTANCE;

    private final Context context;

    private TodoViewModelFactory(Context context) {
        this.context = context;
    }

    public static TodoViewModelFactory getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new TodoViewModelFactory(context);
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == TasksViewModel.class) {
            return (T) new TasksViewModel(
                    new TasksActionProcessorHolder(
                            Injection.provideTasksRepository(context),
                            Injection.provideSchedulerProvider()));
        }
        if(modelClass == TaskDetailViewModel.class) {
            return (T) new TaskDetailViewModel(
                    new TaskDetailActionProcessorHolder(
                            Injection.provideTasksRepository(context),
                            Injection.provideSchedulerProvider()));
        }



        throw  new IllegalArgumentException("wrong model class");
    }
}
