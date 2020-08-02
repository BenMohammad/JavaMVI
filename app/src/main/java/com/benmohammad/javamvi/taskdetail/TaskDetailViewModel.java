package com.benmohammad.javamvi.taskdetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.mvibase.MviIntent;
import com.benmohammad.javamvi.mvibase.MviViewModel;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class TaskDetailViewModel extends ViewModel implements MviViewModel<TaskDetailIntent, TaskDetailViewState> {

    @NonNull
    private PublishSubject<TaskDetailIntent> intentSubject;

    @NonNull
    private Observable<TaskDetailViewState> statesObservable;

    @NonNull
    private CompositeDisposable disposables= new CompositeDisposable();

    @NonNull
    private TaskDetailActionProcessorHolder actionProcessorHolder;

    public TaskDetailViewModel(@NonNull TaskDetailActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = actionProcessorHolder;
        intentSubject = PublishSubject.create();
        statesObservable = compose();
    }

    private ObservableTransformer<TaskDetailIntent, TaskDetailIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(TaskDetailIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof TaskDetailIntent.InitialIntent))
                    ));



    @Override
    public void processIntents(Observable<TaskDetailIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<TaskDetailViewState> states() {
        return statesObservable;
    }



    private Observable<TaskDetailViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::addFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(TaskDetailViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private TaskDetailAction actionFromIntent(MviIntent intent) {
        if(intent instanceof TaskDetailIntent.InitialIntent) {
            String taskId = ((TaskDetailIntent.InitialIntent) intent).taskId();
            checkNotNull(taskId);
            return TaskDetailAction.PopulateTask.create(taskId);
        }

        if(intent instanceof TaskDetailIntent.DeleteTask) {
            TaskDetailIntent.DeleteTask deleteTaskIntent = (TaskDetailIntent.DeleteTask) intent;
            final String taskId = deleteTaskIntent.taskId();
            return TaskDetailAction.DeleteTask.create(taskId);
        }

        if(intent instanceof TaskDetailIntent.CompleteTaskIntent) {
            TaskDetailIntent.CompleteTaskIntent completeTaskIntent = (TaskDetailIntent.CompleteTaskIntent) intent;
            final String taskId =completeTaskIntent.taskId();
            return TaskDetailAction.CompleteTask.create(taskId);
        }

        if(intent instanceof TaskDetailIntent.ActivateTaskIntent) {
            TaskDetailIntent.ActivateTaskIntent activateTaskIntent = (TaskDetailIntent.ActivateTaskIntent) intent;
            final String taskId = activateTaskIntent.taskId();
            return  TaskDetailAction.ActivateTask.create(taskId);
        }

        throw new IllegalArgumentException("do not know how to handle life....");
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<TaskDetailViewState, TaskDetailResult, TaskDetailViewState> reducer =
            (previousState, result) -> {
        TaskDetailViewState.Builder stateBuilder = previousState.buildWith();
        if(result instanceof  TaskDetailResult.PopulateTask) {
            TaskDetailResult.PopulateTask populateTaskResult =
                    (TaskDetailResult.PopulateTask) result;
            switch(populateTaskResult.status()) {
                case SUCCESS:
                    Task task = populateTaskResult.task();
                    stateBuilder.title(task.getTitle());
                    stateBuilder.description(task.getDescription());
                    stateBuilder.active(task.isActive());
                    return stateBuilder.build();
                case FAILURE:
                    Throwable error = populateTaskResult.error();
                    stateBuilder.loading(false);
                    return stateBuilder.error(error).build();
                case IN_FLIGHT:
                    stateBuilder.loading(true);
                    return stateBuilder;
            }
        }
        if(result instanceof TaskDetailResult.DeleteTaskResult) {
            AutoValue_TaskDetailAction_DeleteTask
        }
            }
}
