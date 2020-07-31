package com.benmohammad.javamvi.tasks;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviViewState;
import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class TasksViewState implements MviViewState {

    public abstract boolean isLoading();
    public abstract TaskFilterType taskFilterType();
    public abstract List<Task> tasks();

    @Nullable
    abstract Throwable error();

    public abstract boolean taskComplete();
    public abstract boolean taskActivated();
    public abstract boolean completedTaskCleared();

    public abstract Builder buildWith();

    static TasksViewState idle() {
        return new AutoValue_TasksViewState.Builder().isLoading(false)
                .taskFilterType(TaskFilterType.ALL_TASKS)
                .tasks(Collections.emptyList())
                .error(null)
                .taskComplete(false)
                .taskActivated(false)
                .completedTaskCleared(false)
                .build();
    }



    @AutoValue.Builder
    static abstract class Builder {
        abstract Builder isLoading(boolean isLoading);
        abstract Builder taskFilterType(TaskFilterType taskFilterType);
        abstract Builder tasks(@Nullable List<Task> tasks);
        abstract Builder error(@Nullable Throwable error);
        abstract Builder taskComplete(boolean taskComplete);
        abstract Builder taskActivated(boolean taskActivated);
        abstract Builder completedTaskCleared(boolean completedTAskCleared);

        abstract TasksViewState build();
    }
}
