package com.benmohammad.javamvi.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.data.source.TasksDataSource;
import com.benmohammad.javamvi.data.source.local.TasksPersistenceContract.TaskEntry;
import com.benmohammad.javamvi.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class TasksLocalDataSource implements TasksDataSource {

    @Nullable
    private static TasksLocalDataSource INSTANCE;

    @NonNull
    private final BriteDatabase mDatabaseHelper;

    @NonNull
    private Function<Cursor, Task> mTaskMapperFunction;

    private TasksLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider) {

        checkNotNull(context, "Context cant be null");
        checkNotNull(schedulerProvider, "Scheduler cannot be null");
        TaskDbHelper dbHelper = new TaskDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mTaskMapperFunction = this::getTask;

    }

    @NonNull
    private Task getTask(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TITLE));
        String description = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DESCRIPTION));
        boolean isCompleted = c.getInt(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, itemId, isCompleted);
    }

    public static TasksLocalDataSource getInstance(@NonNull Context context,
                                                   @NonNull BaseSchedulerProvider schedulerProvider) {
        if(INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }


    @Override
    public Single<List<Task>> getTasks() {
        String[] projection = {
                TaskEntry.COLUMN_NAME_ENTRY_ID, TaskEntry.COLUMN_NAME_TITLE, TaskEntry.COLUMN_NAME_DESCRIPTION,
                TaskEntry.COLUMN_NAME_COMPLETED
        };

         String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TaskEntry.TABLE_NAME);
         return mDatabaseHelper.createQuery(TaskEntry.TABLE_NAME, sql)
                 .mapToList(mTaskMapperFunction)
                 .firstOrError();
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        String[] projection = {
                TaskEntry.COLUMN_NAME_ENTRY_ID, TaskEntry.COLUMN_NAME_TITLE, TaskEntry.COLUMN_NAME_DESCRIPTION,
                TaskEntry.COLUMN_NAME_COMPLETED
        };

        String sql = String.format("SELECT %S FROM %s WHERE %s LIKE ?", TextUtils.join(",", projection),
                TaskEntry.TABLE_NAME, TaskEntry.COLUMN_NAME_ENTRY_ID);

        return mDatabaseHelper.createQuery(TaskEntry.TABLE_NAME, sql, taskId)
                .mapToOne(mTaskMapperFunction)
                .firstOrError();
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TaskEntry.COLUMN_NAME_COMPLETED, task.isCompleted());
        mDatabaseHelper.insert(TaskEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        completeTask(task.getId());
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_COMPLETED, true);
        String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        activateTask(task.getId());
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME_COMPLETED, false);

        String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable clearCompletedTask() {
        String selection = TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        mDatabaseHelper.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
        return null;
    }

    @Override
    public void refreshTasks() {

        //none
    }

    @Override
    public void deleteAllTasks() {
        mDatabaseHelper.delete(TaskEntry.TABLE_NAME, null);
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        return null;
    }
}
