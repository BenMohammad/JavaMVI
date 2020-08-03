package com.benmohammad.javamvi.stats;

import androidx.annotation.NonNull;

import com.benmohammad.javamvi.data.Task;
import com.benmohammad.javamvi.data.source.TasksRepository;
import com.benmohammad.javamvi.util.Pair;
import com.benmohammad.javamvi.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class StatisticsActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public StatisticsActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                           @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasks repository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler provider cannot be null");
    }

    private ObservableTransformer<StatisticsAction.LoadStatistics, StatisticsResult.LoadStatistics> loadStatisticsProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.getTasks()
                    .toObservable()
                    .flatMap(Observable::fromIterable)
                    .publish(shared ->
                            Single.zip(
                                    shared.filter(Task::isActive).count(),
                                    shared.filter(Task::isCompleted).count(),
                                    Pair::create).toObservable())

                    .map(pair -> StatisticsResult.LoadStatistics.success(
                            pair.first().intValue(), pair.second().intValue()))

                    .onErrorReturn(StatisticsResult.LoadStatistics::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(StatisticsResult.LoadStatistics.inFlight()));


    ObservableTransformer<StatisticsAction, StatisticsResult> actionProcessor =
            actions -> actions.publish(shared ->
                    shared.ofType(StatisticsAction.LoadStatistics.class).compose(loadStatisticsProcessor)
                    .cast(StatisticsResult.class).mergeWith(
                    shared.filter(v -> !(v instanceof  StatisticsAction.LoadStatistics))
                            .flatMap(w -> Observable.error(
                                    new IllegalArgumentException("Unknown Action type: " + w)))));
}
