package com.benmohammad.javamvi.tasks;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

public interface TasksIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements TasksIntent {
        public static InitialIntent create() {
            return new AutoValue_TasksIntent_InitialIntent();
        }
    }

    @AutoValue
    abstract class RefreshIntent implements TasksIntent {
        abstract boolean forceUpdate();

        public static RefreshIntent create(boolean forceUpdate) {
            return new AutoValue_TasksIntent_RefreshIntent(forceUpdate);
        }
    }
    @AutoValue
    abstract class ActivateTaskIntent implements TasksIntent {
        abstract Task task();

        public static ActivateTaskIntent create(Task task) {
            return new AutoValue_TasksIntent_ActivateTaskIntent(task);
        }
    }


    @AutoValue
    abstract class CompleteTaskIntent implements TasksIntent {
        abstract Task task();

        public static CompleteTaskIntent create(Task task) {
            return new AutoValue_TasksIntent_CompleteTaskIntent(task);
        }
    }

    @AutoValue
    abstract class ClearCompletedTaskIntent implements TasksIntent {
        public static ClearCompletedTaskIntent create() {
            return new AutoValue_TasksIntent_ClearCompletedTaskIntent();
        }
    }

    @AutoValue
    abstract class ChangeFilterIntent implements TasksIntent {
        abstract TaskFilterType filterType();

        public static ChangeFilterIntent create(TaskFilterType filterType) {
            return new AutoValue_TasksIntent_ChangeFilterIntent(filterType);
        }
    }
}
