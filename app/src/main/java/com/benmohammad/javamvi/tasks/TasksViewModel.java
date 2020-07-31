package com.benmohammad.javamvi.tasks;

import androidx.lifecycle.ViewModel;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviIntent;
import com.benmohammad.javamvi.mvibase.MviViewModel;
import com.benmohammad.javamvi.util.UiNotification;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class TasksViewModel extends ViewModel implements MviViewModel<TasksIntent, TasksViewState> {

    @Nonnull
    private PublishSubject<TasksIntent> intentSubject;

    @Nonnull
    private Observable<TasksViewState> statesObservable;

    @Nonnull
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nonnull
    private TasksActionProcessorHolder actionProcessorHolder;

    public TasksViewModel(@Nonnull TasksActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder, "actionProcessorHolder cannot be null");
        intentSubject = PublishSubject.create();
        statesObservable = compose();
    }

    @Override
    public void processIntents(Observable<TasksIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<TasksViewState> states() {
        return statesObservable;
    }

    private Observable<TasksViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(TasksViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private ObservableTransformer<TasksIntent, TasksIntent> intentFilter =
            intents -> intents.publish( shared ->
                    Observable.merge(
                            shared.ofType(TasksIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof  TasksIntent.InitialIntent))));

    private TasksAction actionFromIntent(MviIntent intent) {
        if(intent instanceof TasksIntent.InitialIntent) {
            return TasksAction.LoadTasks.loadAndFilter(true, TaskFilterType.ALL_TASKS);
        }

        if(intent instanceof TasksIntent.ChangeFilterIntent) {
            return TasksAction.LoadTasks.loadAndFilter(false, ((TasksIntent.ChangeFilterIntent) intent).filterType());
        }
        if(intent instanceof TasksIntent.RefreshIntent) {
            return TasksAction.LoadTasks.load(((TasksIntent.RefreshIntent) intent).forceUpdate());
        }

        if(intent instanceof  TasksIntent.ActivateTaskIntent) {
            return TasksAction.ActivateTaskAction.create(
                    ((TasksIntent.ActivateTaskIntent) intent).task());
        }

        if(intent instanceof TasksIntent.CompleteTaskIntent) {
            return TasksAction.CompleteTaskAction.create(
                    ((TasksIntent.CompleteTaskIntent) intent).task());
        }
        if(intent instanceof TasksIntent.ClearCompletedTaskIntent) {
            return TasksAction.ClearCompletedTasksAction.create();
        }
        throw new IllegalArgumentException("do not know how to deal with my life....!");
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<TasksViewState, TasksResult, TasksViewState> reducer =
            (previousState, result) -> {
                TasksViewState.Builder stateBuilder = previousState.buildWith();
                if(result instanceof TasksResult.LoadTasks) {
                    TasksResult.LoadTasks loadResult = (TasksResult.LoadTasks) result;
                    switch(loadResult.status()) {
                        case SUCCESS:
                            TaskFilterType filterType = loadResult.filterType();
                            if(filterType == null) {
                                filterType = previousState.taskFilterType();
                            }
                            List<Task> tasks = filteredTasks(checkNotNull(loadResult.tasks()), filterType);
                            return stateBuilder.isLoading(false).tasks(tasks).taskFilterType(filterType).build();
                        case FAILURE:
                            return stateBuilder.isLoading(false).error(loadResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.isLoading(true).build();
                    }
                } else if(result instanceof TasksResult.CompleteTaskResult) {
                    TasksResult.CompleteTaskResult completeTaskResult =
                            (TasksResult.CompleteTaskResult) result;
                    switch(completeTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskComplete(completeTaskResult.uiNotificationStatus() == UiNotification.SHOW);
                            if(completeTaskResult.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(completeTaskResult.tasks()), previousState.taskFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(completeTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if(result instanceof TasksResult.ActivateTaskResult) {
                    TasksResult.ActivateTaskResult activeTaskResult = (TasksResult.ActivateTaskResult) result;
                    switch(activeTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskActivated(activeTaskResult.uiNotificationStatus() == UiNotification.SHOW);
                            if(activeTaskResult.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(activeTaskResult.tasks()), previousState.taskFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(activeTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if(result instanceof TasksResult.ClearCompletedTaskResult) {
                    TasksResult.ClearCompletedTaskResult clearCompletedTaskResult = (TasksResult.ClearCompletedTaskResult) result;
                    switch(clearCompletedTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.completedTaskCleared(clearCompletedTaskResult.uiNotificationStatus() == UiNotification.SHOW);
                            if(clearCompletedTaskResult.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(clearCompletedTaskResult.tasks()), previousState.taskFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(clearCompletedTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else {
                    throw new IllegalArgumentException("God knows!!");
                }
                throw new IllegalStateException("Mishandled state");
            };

            private static List<Task> filteredTasks(@Nonnull List<Task> tasks,
                                                    @Nonnull TaskFilterType filterType) {
                List<Task> filteredTasks = new ArrayList<>(tasks.size());
                switch(filterType) {
                    case ALL_TASKS:
                        filteredTasks.addAll(tasks);
                        break;
                    case ACTIVE_TASKS:
                        for(Task task: tasks) {
                            if(task.isActive()) filteredTasks.add(task);
                        }
                        break;
                    case COMPLETED_TASKS:
                        for(Task task: tasks) {
                            if(task.isCompleted()) filteredTasks.add(task);
                        }
                        break;
                }
                return filteredTasks;
            }


}
