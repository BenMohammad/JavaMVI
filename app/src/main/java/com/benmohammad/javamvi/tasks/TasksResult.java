package com.benmohammad.javamvi.tasks;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviResult;
import com.benmohammad.javamvi.util.LceStatus;
import com.benmohammad.javamvi.util.UiNotification;
import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


import static com.benmohammad.javamvi.util.LceStatus.FAILURE;
import static com.benmohammad.javamvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javamvi.util.LceStatus.SUCCESS;
import static com.benmohammad.javamvi.util.UiNotification.HIDE;
import static com.benmohammad.javamvi.util.UiNotification.SHOW;

public interface TasksResult extends MviResult {

    @AutoValue
    abstract class LoadTasks implements TasksResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract TaskFilterType filterType();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static LoadTasks success(@Nonnull List<Task> tasks, @Nullable TaskFilterType filterType) {
            return new AutoValue_TasksResult_LoadTasks(SUCCESS, tasks, filterType, null);
        }

        @Nonnull
        static LoadTasks failure(Throwable error) {
            return new AutoValue_TasksResult_LoadTasks(FAILURE, null, null, null);
        }

        @Nonnull
        static LoadTasks inFlight() {
            return new AutoValue_TasksResult_LoadTasks(IN_FLIGHT, null, null, null);
        }
    }
    @AutoValue
    abstract class ActivateTaskResult  implements TasksResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract UiNotification uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static ActivateTaskResult hideUiNotification() {
            return new AutoValue_TasksResult_ActivateTaskResult(SUCCESS, HIDE, null, null);
        }

        @Nonnull
        static ActivateTaskResult success(@Nonnull List<Task> tasks) {
            return new AutoValue_TasksResult_ActivateTaskResult(SUCCESS, SHOW, tasks, null);
        }

        @Nonnull
        static ActivateTaskResult failure(Throwable error) {
            return new AutoValue_TasksResult_ActivateTaskResult(FAILURE, null, null, error);
        }

        @Nonnull
        static ActivateTaskResult inFlight() {
            return new AutoValue_TasksResult_ActivateTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class CompleteTaskResult implements TasksResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract UiNotification uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static CompleteTaskResult hideUiNotification() {
            return new AutoValue_TasksResult_CompleteTaskResult(SUCCESS, HIDE, null, null);
        }

        @Nonnull
        static CompleteTaskResult success(@Nonnull List<Task> tasks) {
            return new AutoValue_TasksResult_CompleteTaskResult(SUCCESS, SHOW, tasks, null);
        }

        @Nonnull
        static CompleteTaskResult failure(Throwable error) {
            return new AutoValue_TasksResult_CompleteTaskResult(FAILURE, null, null, error);
        }

        @Nonnull
        static CompleteTaskResult inFlight() {
            return new AutoValue_TasksResult_CompleteTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class ClearCompletedTaskResult implements TasksResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract UiNotification uiNotificationStatus();

        @Nullable
        abstract List<Task> tasks();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static ClearCompletedTaskResult hideUiNotification() {
            return new AutoValue_TasksResult_ClearCompletedTaskResult(SUCCESS, HIDE, null, null);
        }

        @Nonnull
        static ClearCompletedTaskResult success(@Nonnull List<Task> tasks) {
            return new AutoValue_TasksResult_ClearCompletedTaskResult(SUCCESS, SHOW, tasks, null);
        }

        @Nonnull
        static  ClearCompletedTaskResult failure(Throwable error) {
            return new AutoValue_TasksResult_ClearCompletedTaskResult(FAILURE, null, null, error);
        }

        @Nonnull
        static ClearCompletedTaskResult inFlight() {
            return new AutoValue_TasksResult_ClearCompletedTaskResult(IN_FLIGHT, null, null, null);
        }
    }

}
