package com.benmohammad.javamvi.data.source.remote;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.data.source.TasksDataSource;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class TasksRemoteDataSource implements TasksDataSource {

    private static TasksRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Task> TASK_SERVICE_DATA;

    static {
        TASK_SERVICE_DATA = new LinkedHashMap<>(2);
        addTask("Jon bonJovi", "What a singer!");
        addTask("Pizza", "Eat me....");
    }

    public static TasksRemoteDataSource getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new TasksRemoteDataSource();
        }
        return INSTANCE;
    }

    private TasksRemoteDataSource(){}

    private static void addTask(String title, String description) {
        Task newTask = new Task(title, description);
        TASK_SERVICE_DATA.put(newTask.getId(), newTask);
    }



    @Override
    public Single<List<Task>> getTasks() {
        return Observable.fromIterable(TASK_SERVICE_DATA.values())
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .toList();
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        return Single.just(TASK_SERVICE_DATA.get(taskId))
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        TASK_SERVICE_DATA.put(task.getId(), task);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASK_SERVICE_DATA.put(task.getId(), completedTask);
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASK_SERVICE_DATA.put(task.getId(), activeTask);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        return Completable.complete();
    }

    @Override
    public Completable clearCompletedTask() {
        Iterator<Map.Entry<String, Task>> it = TASK_SERVICE_DATA.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if(entry.getValue().isCompleted()) {
                it.remove();
            }
        }
        return null;
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void deleteAllTasks() {
        TASK_SERVICE_DATA.clear();
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        TASK_SERVICE_DATA.remove(taskId);
        return Completable.complete();
    }
}
