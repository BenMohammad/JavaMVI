package com.benmohammad.javamvi.taskdetail;

import androidx.annotation.NonNull;

import com.benmohammad.javamvi.data.source.TasksRepository;
import com.benmohammad.javamvi.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;
import static autovalue.shaded.com.google$.common.base.$Preconditions.checkPositionIndex;
import static com.benmohammad.javamvi.util.ObservableUtils.pairWithDelay;

public class TaskDetailActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public TaskDetailActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                           @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler provider cannot be null");
    }


    private ObservableTransformer<TaskDetailAction.PopulateTask, TaskDetailResult.PopulateTask> populateTaskProcessor =
            actions -> actions.flatMap(action ->
                   tasksRepository.getTask(action.taskId())
                    .toObservable()
                    .map(TaskDetailResult.PopulateTask::success)
                    .onErrorReturn(TaskDetailResult.PopulateTask::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TaskDetailResult.PopulateTask.inFlight()));


    private ObservableTransformer<TaskDetailAction.CompleteTask, TaskDetailResult.CompleteTaskResult> completeTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.completeTask(action.taskId())
                    .andThen(tasksRepository.getTask(action.taskId()))
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TaskDetailResult.CompleteTaskResult.success(tasks),
                                    TaskDetailResult.CompleteTaskResult.hideUiNotification()
                            )))
            .onErrorReturn(TaskDetailResult.CompleteTaskResult::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(TaskDetailResult.CompleteTaskResult.inFlight());

    private ObservableTransformer<TaskDetailAction.ActivateTask, TaskDetailResult.ActivateTaskResult> activateTaskProcessor =
            actions -> actions.flatMap(action ->
                   tasksRepository.activateTask(action.taskId())
                    .andThen(tasksRepository.getTask(action.taskId())
                            .toObservable()
                            .flatMap(tasks ->
                                    pairWithDelay(
                                            TaskDetailResult.ActivateTaskResult.success(tasks),
                                            TaskDetailResult.ActivateTaskResult.hideUiNotification()
                                    ))
                        .onErrorReturn(TaskDetailResult.ActivateTaskResult::failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(TaskDetailResult.ActivateTaskResult.inFlight())));

            private ObservableTransformer<TaskDetailAction.DeleteTask, TaskDetailResult.DeleteTaskResult> deleteTasKProcessor =
                    actions -> actions.flatMap(
                            action ->
                                    tasksRepository.deleteTask(action.taskId())
                            .andThen(Observable.just(TaskDetailResult.DeleteTaskResult.success()))
                            .onErrorReturn(TaskDetailResult.DeleteTaskResult::failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(TaskDetailResult.DeleteTaskResult.inFlight()));


            ObservableTransformer<TaskDetailAction, TaskDetailResult> actionProcessor =
                    actions -> actions.publish(
                            shared ->
                                    Observable.merge(
                                            shared.ofType(TaskDetailAction.PopulateTask.class).compose(populateTaskProcessor),
                                            shared.ofType(TaskDetailAction.CompleteTask.class).compose(completeTaskProcessor),
                                            shared.ofType(TaskDetailAction.ActivateTask.class).compose(activateTaskProcessor),
                                            shared.ofType(TaskDetailAction.DeleteTask.class).compose(deleteTasKProcessor))

                            .mergeWith(shared.filter(v -> !(v instanceof TaskDetailAction.PopulateTask)

                                                && !(v instanceof TaskDetailAction.CompleteTask)
                                                && !(v instanceof TaskDetailAction.ActivateTask)
                                                && !(v instanceof TaskDetailAction.DeleteTask))
                            .flatMap(w -> Observable.error(
                                    new IllegalArgumentException("Unknown error")
                            ))
                            ));

}
