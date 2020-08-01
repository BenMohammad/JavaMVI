package com.benmohammad.javamvi.taskdetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviResult;
import com.benmohammad.javamvi.util.LceStatus;
import com.benmohammad.javamvi.util.UiNotification;
import com.google.auto.value.AutoValue;

import static com.benmohammad.javamvi.util.LceStatus.FAILURE;
import static com.benmohammad.javamvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javamvi.util.LceStatus.SUCCESS;
import static com.benmohammad.javamvi.util.UiNotification.HIDE;
import static com.benmohammad.javamvi.util.UiNotification.SHOW;

public interface TaskDetailResult extends MviResult {

    @AutoValue
    abstract class PopulateTask implements TaskDetailResult {
        @NonNull
        abstract LceStatus status();
        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @NonNull
        static PopulateTask success(@NonNull Task task) {
            return new AutoValue_TaskDetailResult_PopulateTask(SUCCESS, task, null);
        }

        @NonNull
        static PopulateTask failure(Throwable error) {
            return new AutoValue_TaskDetailResult_PopulateTask(FAILURE, null, error);
        }

        @NonNull
        static PopulateTask inFlight() {
            return new AutoValue_TaskDetailResult_PopulateTask(IN_FLIGHT, null, null);
        }
    }

    @AutoValue
    abstract class ActivateTaskResult implements TaskDetailResult {
        @NonNull
        abstract LceStatus status();
        @Nullable
        abstract UiNotification uiNotificationStatus();
        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @NonNull
        static ActivateTaskResult hideUiNotification() {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(SUCCESS, HIDE, null, null);
        }

        @NonNull
        static ActivateTaskResult success(@NonNull Task task) {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(SUCCESS, SHOW, task, null);
        }


        @NonNull
        static ActivateTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(FAILURE, null, null, error);
        }


        @NonNull
        static ActivateTaskResult inFlight() {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class CompleteTaskResult implements TaskDetailResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract UiNotification uiNotificationStatus();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @NonNull
        static CompleteTaskResult hideUiNotification() {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(SUCCESS, HIDE, null, null);
        }

        @NonNull
        static CompleteTaskResult success(@NonNull Task task) {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(SUCCESS, SHOW, task, null);
        }

        @NonNull
        static CompleteTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(FAILURE, null, null, error);
        }

        @NonNull
        static CompleteTaskResult inFlight() {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class DeleteTaskResult implements TaskDetailResult {
        @NonNull
        abstract LceStatus status();

        @Nullable
        abstract Throwable error();

        @NonNull
        static DeleteTaskResult success() {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(SUCCESS, null);
        }

        @NonNull
        static DeleteTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(SUCCESS, error);
        }

        @NonNull
        static DeleteTaskResult inFlight() {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(IN_FLIGHT, null);
        }
    }
}
