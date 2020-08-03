package com.benmohammad.javamvi.stats;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.benmohammad.javamvi.mvibase.MviIntent;
import com.benmohammad.javamvi.mvibase.MviViewModel;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class StatisticsViewModel extends ViewModel implements MviViewModel<StatisticsIntent, StatisticsViewState> {

    @NonNull
    private PublishSubject<StatisticsIntent> intentSubject;

    @NonNull
    private Observable<StatisticsViewState> stateObservable;

    @NonNull
    private CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private StatisticsActionProcessorHolder actionProcessorHolder;

    public StatisticsViewModel(@NonNull StatisticsActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder, "actionProcessor holder cannot be null");
        intentSubject = PublishSubject.create();
        stateObservable = compose();
    }



    @Override
    public void processIntents(Observable<StatisticsIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<StatisticsViewState> states() {
        return stateObservable;
    }

    private Observable<StatisticsViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(StatisticsViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private ObservableTransformer<StatisticsIntent, StatisticsIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(StatisticsIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof StatisticsIntent.InitialIntent))
                    ));

    private StatisticsAction actionFromIntent(MviIntent intent) {
        if(intent instanceof StatisticsIntent.InitialIntent) {
            return StatisticsAction.LoadStatistics.create();
        }
        throw new IllegalArgumentException("do not know how to handle this intent:" + intent);
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<StatisticsViewState, StatisticsResult, StatisticsViewState> reducer =
            (previousState, result) -> {
        StatisticsViewState.Builder stateBuilder = previousState.buildWith();
        if(result instanceof StatisticsResult.LoadStatistics) {
            StatisticsResult.LoadStatistics loadStatisticsResult = (StatisticsResult.LoadStatistics) result;
            switch(loadStatisticsResult.status()) {
                case SUCCESS:
                    return stateBuilder.isLoading(false)
                            .activeCount(loadStatisticsResult.activeCount())
                            .completedCount(loadStatisticsResult.completedCount())
                            .build();
                case FAILURE:
                    return stateBuilder.isLoading(false).error(loadStatisticsResult.error()).build();
                case IN_FLIGHT:
                    return stateBuilder.isLoading(true).build();
            }
        }else {
            throw new IllegalArgumentException("Don't Know this result");
        }
        throw new IllegalStateException("Mishandled result...");
    };

}
