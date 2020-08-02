package com.benmohammad.javamvi.taskdetail;

import androidx.fragment.app.Fragment;

import com.benmohammad.javamvi.mvibase.MviView;

import io.reactivex.Observable;

public class TaskDetailFragment extends Fragment implements MviView<TaskDetailIntent, TaskDetailViewState> {



    @Override
    public Observable<TaskDetailIntent> intents() {
        return null;
    }

    @Override
    public void render(TaskDetailViewState state) {

    }
}
