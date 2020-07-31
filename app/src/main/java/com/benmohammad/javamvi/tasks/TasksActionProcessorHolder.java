package com.benmohammad.javamvi.tasks;

import com.benmohammad.javamvi.data.source.TasksRepository;
import com.benmohammad.javamvi.tasks.TasksAction.ActivateTaskAction;
import com.benmohammad.javamvi.tasks.TasksAction.ClearCompletedTasksAction;
import com.benmohammad.javamvi.tasks.TasksAction.CompleteTaskAction;
import com.benmohammad.javamvi.tasks.TasksResult.ActivateTaskResult;
import com.benmohammad.javamvi.tasks.TasksResult.ClearCompletedTaskResult;
import com.benmohammad.javamvi.util.schedulers.BaseSchedulerProvider;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;
import static com.benmohammad.javamvi.util.ObservableUtils.pairWithDelay;

public class TasksActionProcessorHolder {

    @Nonnull
    private TasksRepository tasksRepository;

    @Nonnull
    private BaseSchedulerProvider schedulerProvider;

    public TasksActionProcessorHolder(@Nonnull TasksRepository tasksRepository,
                                      @Nonnull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "repository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler cannot be null");
    }

    private ObservableTransformer<TasksAction.LoadTasks, TasksResult.LoadTasks> loadTaskProcessor =
            actions -> actions
            .flatMap(action -> tasksRepository.getTasks(action.forceUpdate())
            .toObservable()
                    .map(tasks -> TasksResult.LoadTasks.success(tasks, action.filterType()))
                    .onErrorReturn(TasksResult.LoadTasks::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.LoadTasks.inFlight()));


    private ObservableTransformer<ActivateTaskAction, ActivateTaskResult> activateTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.activateTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.ActivateTaskResult.success(tasks),
                                    TasksResult.ActivateTaskResult.hideUiNotification()

                            ))
                    .onErrorReturn(TasksResult.ActivateTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ActivateTaskResult.inFlight()));

    private ObservableTransformer<CompleteTaskAction, TasksResult.CompleteTaskResult> completeTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.completeTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.CompleteTaskResult.success(tasks),
                                    TasksResult.CompleteTaskResult.hideUiNotification()
                            ))
                    .onErrorReturn(TasksResult.CompleteTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.CompleteTaskResult.inFlight()));


    private ObservableTransformer<ClearCompletedTasksAction, ClearCompletedTaskResult> clearCompletedTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.clearCompletedTask()
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.ClearCompletedTaskResult.success(tasks),
                                    TasksResult.ClearCompletedTaskResult.hideUiNotification()
                            ))
                    .onErrorReturn(TasksResult.ClearCompletedTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ClearCompletedTaskResult.inFlight()));

    ObservableTransformer<TasksAction, TasksResult> actionProcessor =
            actions -> actions.publish(
                    shared ->
                            Observable.merge(
                                    shared.ofType(TasksAction.LoadTasks.class).compose(loadTaskProcessor),
                                    shared.ofType(TasksAction.ActivateTaskAction.class).compose(activateTaskProcessor),
                                    shared.ofType(CompleteTaskAction.class).compose(completeTaskProcessor),
                                    shared.ofType(ClearCompletedTasksAction.class).compose(clearCompletedTaskProcessor))

                    .mergeWith(
                            shared.filter(v -> !(v instanceof TasksAction.LoadTasks)
                            && !(v instanceof TasksAction.ActivateTaskAction)
                            && !(v instanceof TasksAction.CompleteTaskAction)
                            && !(v instanceof TasksAction.ClearCompletedTasksAction))
                            .flatMap(w ->
                                    Observable.error(
                                            new IllegalArgumentException("Unknown action: " + w)))));
}
