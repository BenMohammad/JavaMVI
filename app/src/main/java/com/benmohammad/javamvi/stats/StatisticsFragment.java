package com.benmohammad.javamvi.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.mvibase.MviView;
import com.benmohammad.javamvi.util.TodoViewModelFactory;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class StatisticsFragment extends Fragment implements MviView<StatisticsIntent, StatisticsViewState> {

    private TextView statisticsTv;
    private StatisticsViewModel viewModel;
    private CompositeDisposable disposables;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.stats_frag, container, false);
        statisticsTv = (TextView) root.findViewById(R.id.statistics);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, TodoViewModelFactory.getInstance(getContext())).get(StatisticsViewModel.class);
        disposables = new CompositeDisposable();
        bind();
    }

    private void bind() {
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public Observable<StatisticsIntent> intents() {
        return initialIntent();
    }

    private Observable<StatisticsIntent> initialIntent() {
        return Observable.just(StatisticsIntent.InitialIntent.create());
    }

    @Override
    public void render(StatisticsViewState state) {
        if(state.isLoading()) statisticsTv.setText(getString(R.string.loading));
        if(state.error() != null) {
            statisticsTv.setText(getResources().getString(R.string.statistics_error));
        }

        if(state.error() == null && !state.isLoading()) {
            showStatistics(state.activeCount(), state.completedCount());
        }
    }

    private void showStatistics(int numberOfActiveCount, int numberOfCompletedCount) {
        if(numberOfCompletedCount == 0 && numberOfActiveCount == 0) {
            statisticsTv.setText(getResources().getString(R.string.statistics_no_tasks));
        } else {
            String displayString = getResources().getString(R.string.statistics_active_tasks)
                    + " "
                    + numberOfActiveCount
                    + "\n"
                    + getResources().getString(R.string.statistics_completed_tasks)
                    + " "
                    + numberOfCompletedCount;
            statisticsTv.setText(displayString);
        }
    }
}
